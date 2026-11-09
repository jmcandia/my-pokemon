# My Pokémon

App Android que muestra un Pokémon aleatorio (ID 1–1025) obtenido de [PokéAPI](https://pokeapi.co/). Cada vez que se pulsa **¡Buscar Pokémon aleatorio!** pide uno nuevo y muestra su imagen, número, nombre, tipos, peso y altura.

Está hecha con **Kotlin**, **Jetpack Compose** (Material 3) y el patrón **MVVM**. La red usa **Retrofit** + **Gson**, y las imágenes se cargan con **Coil**. El layout se adapta al ancho de la pantalla: en teléfono muestra una columna, y en tablet o en horizontal pone los detalles a la izquierda y la imagen a la derecha, centrados.

Este documento explica cómo ejecutar el proyecto y cómo replicarlo desde cero, paso a paso, con especial atención a Retrofit.

---

## Índice

1. [Requisitos](#requisitos)
2. [Ejecutar el proyecto](#ejecutar-el-proyecto)
3. [Arquitectura](#arquitectura)
4. [Replicar el proyecto paso a paso](#replicar-el-proyecto-paso-a-paso)
   - [Paso 1. Crear el proyecto](#paso-1-crear-el-proyecto)
   - [Paso 2. Añadir las dependencias](#paso-2-añadir-las-dependencias)
   - [Paso 3. Manifest: permiso de Internet y Application](#paso-3-manifest-permiso-de-internet-y-application)
   - [Paso 4. Modelos de datos (Gson)](#paso-4-modelos-de-datos-gson)
   - [Paso 5. Retrofit: la capa de red](#paso-5-retrofit-la-capa-de-red)
   - [Paso 6. Repositorio](#paso-6-repositorio)
   - [Paso 7. Estado de la UI](#paso-7-estado-de-la-ui)
   - [Paso 8. ViewModel](#paso-8-viewmodel)
   - [Paso 9. Interfaz con Compose](#paso-9-interfaz-con-compose)
   - [Paso 10. Application y MainActivity](#paso-10-application-y-mainactivity)
5. [Testear con piezas falsas](#testear-con-piezas-falsas)
6. [Comandos útiles](#comandos-útiles)

---

## Requisitos

- Android Studio reciente, compatible con Android Gradle Plugin 9.5.
- El JDK que trae Android Studio. El código se compila con destino Java 11.
- Un emulador o dispositivo con **Android 11 (API 30)** o superior, con conexión a Internet.

## Ejecutar el proyecto

1. Abre la carpeta del proyecto en Android Studio y espera a que termine la sincronización de Gradle.
2. Elige un emulador o dispositivo y pulsa **Run**, o usa la terminal:

```bash
./gradlew installDebug      # en Windows (PowerShell): .\gradlew.bat installDebug
```

---

## Arquitectura

El proyecto sigue **MVVM** (Model–View–ViewModel), con capas simples y sin framework de inyección de dependencias:

```
┌───────────────────────── View (Compose) ─────────────────────────┐
│ PokemonScreen ──► PokemonCompactScreen / PokemonMediumScreen     │
│                        └──► PokemonStateLayout + componentes     │
└──────────────▲───────────────────────────────────┬───────────────┘
               │ uiState (estado)                  │ onRandomClick (evento)
┌──────────────┴───────────────────────────────────▼───────────────┐
│ ViewModel: PokemonViewModel  (Pokemon ──toUi()──► PokemonUi)     │
└──────────────────────────────────┬───────────────────────────────┘
                                   │ getPokemon(id) / PokemonException
┌──────────────────────────────────▼───────────────────────────────┐
│ Model: PokemonRepository (interfaz)                              │
│          └─ DefaultPokemonRepository ──► ApiService ──► PokéAPI  │
└──────────────────────────────────────────────────────────────────┘
```

- El **estado baja** (`uiState`) y los **eventos suben** (`onRandomClick`). Es un flujo unidireccional.
- Solo `PokemonScreen` conoce el ViewModel. El resto de composables reciben datos y lambdas, así que se pueden previsualizar sin red.
- **Cada capa solo conoce a la de abajo, y por su contrato.** La View recibe un `PokemonUi` ya formateado, no el DTO de Gson. El ViewModel depende de la interfaz `PokemonRepository` y solo ve `PokemonException`, nunca Retrofit. Los textos están en `strings.xml`: el ViewModel entrega un `UiText` (ID de recurso + argumentos) y la View lo resuelve.
- Las dependencias se pasan **por constructor** y se crean en un solo sitio, el `AppContainer`, que vive en `PokemonApplication`. Eso permite testear cada capa con piezas falsas.

Estructura de carpetas (`app/src/main/java/com/example/mypokemon/`):

```
MainActivity.kt                 Punto de entrada: tema + PokemonScreen
PokemonApplication.kt           Crea el AppContainer al arrancar la app
data/
  AppContainer.kt               Contenedor de dependencias (inyección manual)
  remote/ApiService.kt          Interfaz Retrofit (endpoints)
  remote/RetrofitInstance.kt    Configuración de Retrofit (singleton)
  repository/
    PokemonRepository.kt        Contrato que usa el ViewModel
    DefaultPokemonRepository.kt Implementación con Retrofit
    PokemonException.kt         Errores del repositorio: Network / Server / Unknown
model/
  Pokemon.kt, Sprites.kt,       DTOs: clases que Gson rellena con el JSON
  OtherSprites.kt, OfficialArtwork.kt,
  Types.kt, Type.kt
viewmodel/
  PokemonViewModel.kt           Estado + lógica + Factory
  PokemonUiState.kt             Estados de pantalla: Loading / Success / Error
  PokemonUi.kt                  Modelo listo para mostrar + conversión desde el DTO
ui/
  screen/                       Pantallas y layout común
  components/                   Imagen, detalles y etiqueta de tipo
  theme/                        Tema, colores y tipografía
  utils/Utils.kt                Clase de tamaño de ventana
  utils/UiText.kt               Texto sin resolver (recurso + argumentos)
res/values/strings.xml          Todos los textos de la app
```

---

## Replicar el proyecto paso a paso

### Paso 1. Crear el proyecto

En Android Studio: **File ▸ New ▸ New Project ▸ Empty Activity** (la plantilla de Compose).

- **Name:** `My Pokémon`
- **Package name:** `com.example.mypokemon`
- **Minimum SDK:** API 30 (Android 11)
- **Build configuration language:** Kotlin DSL

### Paso 2. Añadir las dependencias

El proyecto usa un *version catalog*. Añade esto a `gradle/libs.versions.toml`:

```toml
[versions]
retrofit = "3.0.0"
converterGson = "3.0.0"
coilCompose = "3.6.3"
lifecycleRuntimeKtx = "2.11.0"
material3WindowSizeClass = "1.4.0"
coroutinesTest = "1.10.2"

[libraries]
retrofit = { module = "com.squareup.retrofit2:retrofit", version.ref = "retrofit" }
converter-gson = { module = "com.squareup.retrofit2:converter-gson", version.ref = "converterGson" }
coil-compose = { module = "io.coil-kt.coil3:coil-compose", version.ref = "coilCompose" }
coil-network-okhttp = { module = "io.coil-kt.coil3:coil-network-okhttp", version.ref = "coilCompose" }
androidx-lifecycle-viewmodel-compose = { module = "androidx.lifecycle:lifecycle-viewmodel-compose", version.ref = "lifecycleRuntimeKtx" }
androidx-lifecycle-runtime-compose = { module = "androidx.lifecycle:lifecycle-runtime-compose", version.ref = "lifecycleRuntimeKtx" }
kotlinx-coroutines-test = { module = "org.jetbrains.kotlinx:kotlinx-coroutines-test", version.ref = "coroutinesTest" }
androidx-compose-material3-window-size-class1 = { group = "androidx.compose.material3", name = "material3-window-size-class", version.ref = "material3WindowSizeClass" }
```

Y en `app/build.gradle.kts`, dentro de `dependencies { }`:

```kotlin
implementation(libs.retrofit)                                   // Cliente HTTP declarativo
implementation(libs.converter.gson)                             // JSON ⇄ clases Kotlin
implementation(libs.coil.compose)                               // Carga de imágenes en Compose
implementation(libs.coil.network.okhttp)                        // Descarga de imágenes por HTTP
implementation(libs.androidx.lifecycle.viewmodel.compose)       // viewModel() en Compose
implementation(libs.androidx.lifecycle.runtime.compose)         // collectAsStateWithLifecycle()
implementation(libs.androidx.compose.material3.window.size.class1) // Layout adaptativo
testImplementation(libs.kotlinx.coroutines.test)                // runTest y dispatchers de test
```

| Librería | Para qué se usa |
|---|---|
| `retrofit` | Convierte una interfaz Kotlin anotada en llamadas HTTP. |
| `converter-gson` | Le dice a Retrofit cómo convertir el JSON de la respuesta en objetos Kotlin. |
| `coil-compose` + `coil-network-okhttp` | Descargan y muestran el sprite del Pokémon. Coil 3 necesita el módulo de red aparte. |
| `lifecycle-viewmodel-compose` | Da la función `viewModel()` para obtener el ViewModel desde un composable. |
| `lifecycle-runtime-compose` | Da `collectAsStateWithLifecycle()`, que convierte el `StateFlow` del ViewModel en estado de Compose. |
| `material3-window-size-class` | Calcula si la pantalla es Compact, Medium o Expanded. |
| `kotlinx-coroutines-test` (solo tests) | `runTest` y dispatchers de test para probar código con corrutinas. |

### Paso 3. Manifest: permiso de Internet y Application

Sin el permiso de Internet, cualquier llamada de red falla. Además, `android:name` indica a Android que use nuestra `Application` propia (se crea en el [paso 10](#paso-10-application-y-mainactivity)). En `app/src/main/AndroidManifest.xml`:

```xml
<uses-permission android:name="android.permission.INTERNET" />

<application
    android:name=".PokemonApplication"
    ... >
```

### Paso 4. Modelos de datos (Gson)

Primero hay que ver qué devuelve la API. Esta es una versión reducida de `GET https://pokeapi.co/api/v2/pokemon/25`:

```json
{
  "id": 25,
  "name": "pikachu",
  "height": 4,
  "weight": 60,
  "sprites": {
    "front_default": "https://.../pokemon/25.png",
    "other": {
      "official-artwork": { "front_default": "https://.../official-artwork/25.png" }
    }
  },
  "types": [
    { "slot": 1, "type": { "name": "electric", "url": "https://pokeapi.co/api/v2/type/13/" } }
  ]
}
```

Cada objeto JSON se convierte en una `data class` (`model/`). Solo hace falta declarar los campos que se usan: Gson ignora el resto.

```kotlin
data class Pokemon(
    @SerializedName("id") val id: Int? = null,
    @SerializedName("name") val name: String? = null,
    @SerializedName("height") val height: Int? = null,   // decímetros
    @SerializedName("weight") val weight: Int? = null,   // hectogramos
    @SerializedName("sprites") val sprites: Sprites? = Sprites(),
    @SerializedName("types") val types: ArrayList<Types> = arrayListOf()
) {
    // Ilustración oficial si existe; si no, el sprite pixel art.
    val imageUrl: String?
        get() = sprites?.other?.officialArtwork?.frontDefault ?: sprites?.frontDefault
}

data class Sprites(
    @SerializedName("other") val other: OtherSprites? = null,
    @SerializedName("front_default") val frontDefault: String? = null /* … */
)
data class OtherSprites(@SerializedName("official-artwork") val officialArtwork: OfficialArtwork? = null)
data class OfficialArtwork(@SerializedName("front_default") val frontDefault: String? = null)
data class Types(@SerializedName("slot") val slot: Int? = null, @SerializedName("type") val type: Type? = Type())
data class Type(@SerializedName("name") val name: String? = null, @SerializedName("url") val url: String? = null)
```

Puntos clave:

- `@SerializedName("front_default")` enlaza la clave del JSON (snake_case) con la propiedad Kotlin (`frontDefault`).
- **Todos los parámetros tienen valor por defecto.** Así Kotlin genera un constructor sin parámetros, Gson lo usa, y las claves que falten en el JSON conservan su valor por defecto.
- **Los campos son nullable.** Gson no respeta la nulabilidad de Kotlin: si el JSON trae `"name": null`, asigna `null` aunque el tipo sea `String`, y la app falla después. Declararlos nullable obliga a la UI a manejar ese caso (`?.`, `?:`).
- La estructura anidada del JSON (`types[].type.name`) se reproduce con clases anidadas (`Types` → `Type`).
- `@SerializedName` también sirve para claves que no son nombres válidos en Kotlin, como `official-artwork` (lleva guion).
- `imageUrl` es una propiedad calculada, sin campo propio, así que Gson la ignora. Prefiere la ilustración oficial (unos 475 px) al sprite `front_default` (96 px), que se ve borroso a 200 dp.

### Paso 5. Retrofit: la capa de red

**Retrofit** es una librería de Square que convierte una **interfaz Kotlin** en un cliente HTTP. Tú declaras *qué* peticiones existen con anotaciones, y Retrofit genera en tiempo de ejecución el código que las hace. Por debajo usa **OkHttp** para la conexión y un **converter** (aquí Gson) para traducir el JSON.

Para usar Retrofit hacen falta tres piezas:

1. Una **interfaz** que describe los endpoints (`ApiService`).
2. Una **instancia** configurada de `Retrofit` (`RetrofitInstance`).
3. Un **converter** que transforma el cuerpo de la respuesta (`GsonConverterFactory`).

#### 5.1. La interfaz: `data/remote/ApiService.kt`

```kotlin
interface ApiService {

    @GET("pokemon/{idOrName}")
    suspend fun getPokemon(
        @Path("idOrName") idOrName: String
    ): Response<Pokemon>
}
```

Cada parte de la firma tiene un significado para Retrofit:

| Elemento | Significado |
|---|---|
| `@GET("pokemon/{idOrName}")` | Método HTTP y ruta. La ruta es **relativa** a la `baseUrl`, así que la URL final es `https://pokeapi.co/api/v2/pokemon/{idOrName}`. |
| `{idOrName}` + `@Path("idOrName")` | Marcador en la ruta. Retrofit lo sustituye por el valor del parámetro: `getPokemon("25")` → `.../pokemon/25`. |
| `suspend` | La función es una corrutina. Retrofit hace la petición en un hilo de fondo y la función se suspende hasta tener respuesta, sin bloquear la UI. |
| `Response<Pokemon>` | Envuelve la respuesta completa: código HTTP, cabeceras y cuerpo ya convertido a `Pokemon`. |

**¿Por qué `Response<Pokemon>` y no `Pokemon`?** Las dos formas son válidas y se diferencian en cómo llegan los errores HTTP:

| Tipo de retorno | Si el servidor responde 404 / 500 |
|---|---|
| `Pokemon` | Retrofit **lanza** una `HttpException`. |
| `Response<Pokemon>` | **No lanza**: devuelve un objeto con `isSuccessful == false` y `body() == null`. |

Aquí se usa `Response<T>` para que el repositorio decida qué hacer con cada respuesta. Si no es correcta, la convierte en `PokemonException.Server` con el código HTTP (ver [5.4](#54-tipos-de-error-y-dónde-se-manejan)). Además cubre el caso de un 200 sin cuerpo, que con `Pokemon` como retorno no se podría controlar.

Otras anotaciones habituales, por si quieres añadir endpoints:

```kotlin
// Parámetros de consulta: GET pokemon?limit=20&offset=40
@GET("pokemon")
suspend fun getPokemonList(
    @Query("limit") limit: Int,
    @Query("offset") offset: Int
): Response<PokemonListResponse>

// Envío de un cuerpo JSON (PokéAPI es de solo lectura; ejemplo genérico)
@POST("items")
suspend fun createItem(@Body item: Item): Response<Item>
```

#### 5.2. La instancia: `data/remote/RetrofitInstance.kt`

```kotlin
object RetrofitInstance {
    private const val BASE_URL = "https://pokeapi.co/api/v2/"

    val api: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)                                   // 1. URL base
            .addConverterFactory(GsonConverterFactory.create())  // 2. JSON ⇄ Kotlin con Gson
            .build()                                             // 3. Crea el cliente
            .create(ApiService::class.java)                      // 4. Implementa la interfaz
    }
}
```

1. **`baseUrl`** debe terminar en `/`. Retrofit lanza `IllegalArgumentException` si no termina así. Las rutas de `@GET` **no** empiezan con `/`: una ruta con `/` inicial se resuelve desde la raíz del dominio y pierde el `api/v2/`.
2. **`addConverterFactory`** registra Gson. Sin un converter, Retrofit no sabe crear un `Pokemon` a partir del JSON.
3. **`build()`** crea el objeto `Retrofit`. Por defecto usa un `OkHttpClient` nuevo.
4. **`create(ApiService::class.java)`** genera un objeto que implementa `ApiService`. Al llamar a `api.getPokemon("25")`, Retrofit arma la URL, hace la petición y convierte la respuesta.

Se usa un `object` (singleton) con `by lazy` por dos razones: crear el cliente es costoso, y así se hace **una sola vez** y solo cuando se usa por primera vez.

#### 5.3. Qué pasa en una llamada

```
api.getPokemon("25")
  │
  ├─ Retrofit arma la URL:  https://pokeapi.co/api/v2/ + pokemon/25
  ├─ OkHttp hace GET en un hilo de fondo (la corrutina queda suspendida)
  ├─ Llega la respuesta HTTP 200 con JSON
  ├─ GsonConverterFactory convierte el JSON en Pokemon(...)
  └─ La corrutina se reanuda con Response<Pokemon>
```

#### 5.4. Tipos de error y dónde se manejan

Las excepciones de Retrofit, OkHttp y Gson no salen de la capa de datos. `DefaultPokemonRepository` las captura y las traduce a una `PokemonException`. El ViewModel solo conoce esos tres tipos y elige un mensaje de `strings.xml` para cada uno:

| Situación | Excepción original | La traduce a | Mensaje en pantalla |
|---|---|---|---|
| Sin conexión, timeout, DNS | `IOException` (OkHttp) | `PokemonException.Network` | "Sin conexión. Revisa tu red e inténtalo de nuevo." |
| El servidor responde 404, 500… o sin cuerpo | Respuesta no exitosa | `PokemonException.Server(code)` | "El servidor respondió con error (404)." |
| JSON con formato inesperado | `JsonSyntaxException` u otra (Gson) | `PokemonException.Unknown` | "Respuesta inesperada del servidor." |
| La corrutina se cancela | `CancellationException` | Ninguna: se relanza | Ninguno. |

Tres reglas que conviene respetar al replicarlo:

1. **Relanzar `CancellationException`.** Cuando el ViewModel se destruye, `viewModelScope` cancela sus corrutinas lanzando esta excepción. Un `catch (e: Exception)` la atraparía y la corrutina seguiría ejecutándose. Por eso el repositorio la captura primero y la relanza. El ViewModel solo captura `PokemonException`, así que la cancelación pasa sin tocarla.
2. **Ordenar los `catch` de lo específico a lo general.** `catch (e: Exception)` va al final; si fuera primero, atraparía todo.
3. **No mostrar `e.message` al usuario.** Suele estar en inglés, es técnico (`Unable to resolve host "pokeapi.co"…`) y puede ser `null`.

#### 5.5. Extensiones opcionales

- **Ver las peticiones en Logcat.** Añade `com.squareup.okhttp3:logging-interceptor` y pasa un cliente propio:

  ```kotlin
  val client = OkHttpClient.Builder()
      .addInterceptor(HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY })
      .build()

  Retrofit.Builder()
      .baseUrl(BASE_URL)
      .client(client)
      .addConverterFactory(GsonConverterFactory.create())
      .build()
  ```

- **R8 en release.** Gson usa reflexión para leer los nombres de los campos. Si R8 llega a ofuscar el paquete `model`, el JSON deja de mapearse y los campos quedan en `null`. En ese caso, añade a `app/src/main/keepRules/rules.keep`:

  ```
  -keep class com.example.mypokemon.model.** { *; }
  ```

### Paso 6. Repositorio

El repositorio es el intermediario entre el ViewModel y la API. Tiene tres piezas en `data/repository/`.

**El contrato, `PokemonRepository.kt`.** Es lo único que conoce el ViewModel:

```kotlin
interface PokemonRepository {
    suspend fun getPokemon(idOrName: String): Pokemon
}
```

**Los errores, `PokemonException.kt`.** Son los únicos que puede lanzar el repositorio:

```kotlin
sealed class PokemonException(cause: Throwable? = null) : Exception(cause) {
    class Network(cause: Throwable) : PokemonException(cause)
    class Server(val code: Int) : PokemonException()
    class Unknown(cause: Throwable) : PokemonException(cause)
}
```

**La implementación, `DefaultPokemonRepository.kt`.** Recibe `ApiService` **por constructor**, en lugar de usar `RetrofitInstance` directamente, para poder sustituirlo por una versión falsa en los tests:

```kotlin
class DefaultPokemonRepository(
    private val api: ApiService
) : PokemonRepository {
    override suspend fun getPokemon(idOrName: String): Pokemon {
        val response = try {
            api.getPokemon(idOrName)
        } catch (e: CancellationException) {
            throw e
        } catch (e: IOException) {
            throw PokemonException.Network(e)
        } catch (e: Exception) {
            throw PokemonException.Unknown(e)
        }
        return response.body()?.takeIf { response.isSuccessful }
            ?: throw PokemonException.Server(response.code())
    }
}
```

- Si la respuesta es correcta y tiene cuerpo, devuelve el `Pokemon`.
- Si no, lanza `PokemonException.Server` con el código HTTP, para que el ViewModel pueda mostrarlo.
- Los errores de red y de Gson se traducen a `Network` y `Unknown`. Así el ViewModel no importa nada de Retrofit (ver [5.4](#54-tipos-de-error-y-dónde-se-manejan)).
- **¿Por qué una interfaz?** El ViewModel depende del contrato, no de Retrofit. En sus tests se le pasa un `FakePokemonRepository`, sin red ni respuestas HTTP falsas.

### Paso 7. Estado de la UI

El estado vive en `viewmodel/`, junto al ViewModel que lo produce. Tiene tres piezas.

**`ui/utils/UiText.kt`: textos sin resolver.** El ViewModel no tiene `Context`, así que no puede leer `strings.xml`. En su lugar indica qué recurso usar y con qué argumentos. La View lo resuelve con `asString()`:

```kotlin
sealed interface UiText {
    data class Plain(val value: String) : UiText
    data class Resource(@StringRes val id: Int, val args: List<Any> = emptyList()) : UiText
}

@Composable
fun UiText.asString(): String = when (this) {
    is UiText.Plain -> value
    is UiText.Resource -> stringResource(id, *args.toTypedArray())
}
```

Todos los textos (botón, errores, título, tipos) están en `res/values/strings.xml`. Para traducir la app basta con añadir `res/values-en/strings.xml`.

**`viewmodel/PokemonUi.kt`: el Pokémon listo para mostrar.** El DTO `Pokemon` tiene campos nullable y unidades de PokéAPI. `toUi()` lo convierte en un modelo que la View pinta tal cual:

```kotlin
data class PokemonUi(
    val title: UiText,          // "#25 - Pikachu" o "Pokémon desconocido"
    val imageUrl: String?,      // artwork oficial o, si falta, el sprite
    val types: List<PokemonTypeUi>,
    val weight: String,         // "6,0 kg" o "—"
    val height: String          // "0,4 m" o "—"
)

data class PokemonTypeUi(val key: String, val label: UiText)  // "electric", "Eléctrico"
```

- `formatTenths` divide por 10 (PokéAPI usa hectogramos y decímetros) y usa el separador decimal del idioma (`6,0 kg` en español). Si falta un dato devuelve `—`.
- `formatTitle` maneja la falta de número o de nombre.
- `typeUi` traduce el tipo con su recurso (`R.string.type_electric`). Un tipo desconocido queda capitalizado. `key` es el nombre en inglés; la View lo usa para elegir el color.

**`viewmodel/PokemonUiState.kt`: los tres estados de la pantalla.**

```kotlin
sealed class PokemonUiState {
    object Loading : PokemonUiState()
    data class Success(val pokemon: PokemonUi) : PokemonUiState()
    data class Error(val message: UiText) : PokemonUiState()
}
```

Al ser `sealed`, un `when (uiState)` sin los tres casos no compila. Así ningún estado queda sin manejar.

### Paso 8. ViewModel

`viewmodel/PokemonViewModel.kt` guarda el estado y contiene la lógica:

```kotlin
class PokemonViewModel(
    private val repository: PokemonRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow<PokemonUiState>(PokemonUiState.Loading)
    val uiState: StateFlow<PokemonUiState> = _uiState.asStateFlow()

    private var loadJob: Job? = null

    init { getRandomPokemon() }

    fun getRandomPokemon() {
        if (loadJob?.isActive == true) return

        loadJob = viewModelScope.launch {
            _uiState.value = PokemonUiState.Loading
            _uiState.value = try {
                val randomId = Random.nextInt(1, 1026).toString()
                PokemonUiState.Success(repository.getPokemon(randomId).toUi())
            } catch (e: PokemonException) {
                PokemonUiState.Error(e.toMessage())
            }
        }
    }

    private fun PokemonException.toMessage(): UiText = when (this) {
        is PokemonException.Network -> UiText.Resource(R.string.error_network)
        is PokemonException.Server -> UiText.Resource(R.string.error_server, listOf(code))
        is PokemonException.Unknown -> UiText.Resource(R.string.error_unknown)
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = this[APPLICATION_KEY] as PokemonApplication
                PokemonViewModel(application.container.pokemonRepository)
            }
        }
    }
}
```

- **`StateFlow`**: el estado se guarda en un `MutableStateFlow` privado y se expone como `StateFlow` de solo lectura. Así solo el ViewModel lo cambia, y el ViewModel no depende de Compose: en los tests basta con leer `uiState.value`.
- **`loadJob`**: guarda la petición en curso. Si llega otra llamada antes de que termine, se ignora. La UI además desactiva el botón mientras carga.
- `viewModelScope.launch` lanza una corrutina que se cancela sola si el ViewModel se destruye. Aquí se llama a la función `suspend` del repositorio.
- **`toUi()`**: convierte el DTO en un `PokemonUi` listo para mostrar. La View nunca ve el `Pokemon` de Gson.
- **Un solo `catch`**: el repositorio solo lanza `PokemonException`, y `toMessage()` elige el recurso de `strings.xml` para cada tipo. Al ser `sealed`, el `when` no compila si se añade un tipo nuevo sin mensaje. El ViewModel no importa nada de Retrofit (ver [5.4](#54-tipos-de-error-y-dónde-se-manejan)).
- **`Factory`**: Android crea los ViewModel con un constructor sin parámetros. Como este recibe un repositorio, la `Factory` le indica cómo construirlo. No crea dependencias: las pide al `AppContainer` de la app (`APPLICATION_KEY` da acceso a la `Application`).

### Paso 9. Interfaz con Compose

**`ui/screen/PokemonScreen.kt`**: punto de entrada de la UI. Obtiene el ViewModel con la `Factory`, convierte el `StateFlow` en estado de Compose y elige el layout según el ancho de la ventana:

```kotlin
@Composable
fun PokemonScreen(
    viewModel: PokemonViewModel = viewModel(factory = PokemonViewModel.Factory)
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(topBar = { /* logo + título */ }) { innerPadding ->
        when (getWindowSizeClass().widthSizeClass) {
            WindowWidthSizeClass.Compact -> PokemonCompactScreen(
                uiState = uiState,
                onRandomClick = viewModel::getRandomPokemon,
                modifier = Modifier.padding(innerPadding)
            )
            WindowWidthSizeClass.Medium,
            WindowWidthSizeClass.Expanded -> PokemonMediumScreen(/* mismos parámetros */)
        }
    }
}
```

`collectAsStateWithLifecycle()` (de `lifecycle-runtime-compose`) recompone la UI cada vez que cambia el estado, y deja de recolectar cuando la app pasa a segundo plano.

**`ui/screen/PokemonStateLayout.kt`**: layout común. Resuelve el `when (uiState)` (spinner, mensaje de error) y pinta el botón, que se desactiva mientras el estado es `Loading`. Cada pantalla solo aporta cómo se ve el estado `Success`:

```kotlin
PokemonStateLayout(uiState = uiState, onRandomClick = onRandomClick, modifier = modifier) { pokemon ->
    // Layout propio de cada pantalla con PokemonImage y PokemonDetails
}
```

- **`PokemonCompactScreen`**: columna con la imagen arriba y los detalles abajo.
- **`PokemonMediumScreen`**: fila con los detalles a la izquierda y la imagen a la derecha. La fila no usa pesos: `Arrangement.spacedBy(48.dp, Alignment.CenterHorizontally)` separa los dos elementos y centra el grupo en la pantalla.

**Componentes (`ui/components/`)**:

- `PokemonImage`: `SubcomposeAsyncImage` de Coil, con spinner mientras carga y el logo como imagen de error. Recibe `pokemon.imageUrl`: la ilustración oficial o, si no existe, el sprite.
- `PokemonDetails`: recibe un `PokemonUi` y muestra el título, los tipos, y el peso y la altura. No formatea nada: los textos llegan listos y solo los resuelve con `asString()` y `stringResource()`.
- `PokemonTypePill`: recibe un `PokemonTypeUi`. Muestra el nombre ya traducido y elige el color con `PokemonTypeColors` según `key`. El texto es blanco o negro según el fondo: `readableTextColor` elige el que tenga más contraste con la fórmula de WCAG, así todos los tipos superan 4.5:1.

Como las pantallas no conocen el ViewModel, sus `@Preview` usan datos fijos (`previewPokemon`, un `Pokemon` convertido con `toUi()`) y se ven sin red.

### Paso 10. Application y MainActivity

**`data/AppContainer.kt`**: el único sitio que decide qué implementación se usa para cada dependencia (inyección manual, sin Hilt ni Koin):

```kotlin
interface AppContainer {
    val pokemonRepository: PokemonRepository
}

class DefaultAppContainer : AppContainer {
    override val pokemonRepository: PokemonRepository by lazy {
        DefaultPokemonRepository(RetrofitInstance.api)
    }
}
```

**`PokemonApplication.kt`**: crea el contenedor una sola vez al arrancar la app. Se registra en el manifest con `android:name` (ver [paso 3](#paso-3-manifest-permiso-de-internet-y-application)):

```kotlin
class PokemonApplication : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = DefaultAppContainer()
    }
}
```

Si mañana se añade una caché o una base de datos, solo cambia `DefaultAppContainer`. El ViewModel y su `Factory` no se tocan.

**`MainActivity.kt`**:

```kotlin
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyPokémonTheme {
                PokemonScreen()
            }
        }
    }
}
```

---

## Testear con piezas falsas

Gracias a la inyección por constructor, cada capa se prueba sin red y con una pieza falsa de la capa de abajo: el repositorio con un `ApiService` falso, y el ViewModel con un repositorio falso. Los tests están en `app/src/test/java/com/example/mypokemon/` y se ejecutan con `./gradlew testDebugUnitTest`.

| Archivo | Qué prueba |
|---|---|
| `FakeApiService.kt` | No es un test: es un `ApiService` falso para los tests del repositorio. Cada test decide qué responde y puede revisar los IDs pedidos. |
| `FakePokemonRepository.kt` | No es un test: es un `PokemonRepository` falso para los tests del ViewModel. Devuelve un `Pokemon` o lanza una `PokemonException`. |
| `MainDispatcherRule.kt` | Regla de JUnit que reemplaza `Dispatchers.Main`, que no existe fuera de Android y que `viewModelScope` necesita. |
| `data/repository/PokemonRepositoryTest.kt` | Respuesta correcta, y la traducción de cada fallo: 404 y 200 sin cuerpo a `Server`, error de red a `Network`, JSON ilegible a `Unknown`. |
| `viewmodel/PokemonViewModelTest.kt` | Paso de `Loading` a `Success`, ID entre 1 y 1025, mensaje para cada `PokemonException`, peticiones simultáneas ignoradas y reintento. |
| `viewmodel/PokemonUiTest.kt` | Conversión a `PokemonUi`: decimales según el idioma, datos faltantes, título y traducción de tipos. |
| `ui/components/PokemonTypePillContrastTest.kt` | Todos los tipos cumplen el contraste mínimo de WCAG AA. |

Los tests comparan IDs de recurso (`R.string.error_network`), no textos. Por eso no se rompen si cambia la redacción de un mensaje.

El `ApiService` falso:

```kotlin
class FakeApiService(
    var handler: suspend (idOrName: String) -> Response<Pokemon>
) : ApiService {
    val requestedIds = mutableListOf<String>()

    override suspend fun getPokemon(idOrName: String): Response<Pokemon> {
        requestedIds += idOrName
        return handler(idOrName)
    }
}
```

Un test del repositorio:

```kotlin
@Test
fun `lanza Server con el código si el servidor responde 404`() = runTest {
    val repository = DefaultPokemonRepository(
        FakeApiService { Response.error(404, "".toResponseBody()) }
    )
    val error = runCatching { repository.getPokemon("25") }.exceptionOrNull()
    assertEquals(404, (error as PokemonException.Server).code)
}
```

Un test del ViewModel, con el repositorio falso. `MainDispatcherRule` usa un `StandardTestDispatcher`, que no ejecuta las corrutinas hasta llamar a `advanceUntilIdle()`. Por eso se puede ver el estado `Loading` antes de que llegue la respuesta:

```kotlin
@get:Rule
val mainDispatcherRule = MainDispatcherRule()

@Test
fun `empieza en Loading y pasa a Success con el Pokémon listo para mostrar`() = runTest {
    val viewModel = PokemonViewModel(FakePokemonRepository { pikachu })

    assertEquals(PokemonUiState.Loading, viewModel.uiState.value)
    advanceUntilIdle()
    assertEquals(PokemonUiState.Success(pikachu.toUi()), viewModel.uiState.value)
}
```

Para escribir estos tests hace falta la dependencia `kotlinx-coroutines-test` (`runTest`, `advanceUntilIdle`). `toResponseBody()` viene de `okhttp3.ResponseBody.Companion`; OkHttp ya llega como dependencia de Retrofit.

---

## Comandos útiles

En PowerShell usa `.\gradlew.bat` en lugar de `./gradlew`.

| Comando | Qué hace |
|---|---|
| `./gradlew assembleDebug` | Compila el APK de debug. |
| `./gradlew installDebug` | Instala la app en el emulador o dispositivo conectado. |
| `./gradlew testDebugUnitTest` | Ejecuta los tests unitarios. |
| `./gradlew connectedDebugAndroidTest` | Ejecuta los tests instrumentados (requiere dispositivo). |
| `./gradlew lint` | Ejecuta el análisis de Android Lint. |
