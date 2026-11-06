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
   - [Paso 3. Permiso de Internet](#paso-3-permiso-de-internet)
   - [Paso 4. Modelos de datos (Gson)](#paso-4-modelos-de-datos-gson)
   - [Paso 5. Retrofit: la capa de red](#paso-5-retrofit-la-capa-de-red)
   - [Paso 6. Repositorio](#paso-6-repositorio)
   - [Paso 7. Estado de la UI](#paso-7-estado-de-la-ui)
   - [Paso 8. ViewModel](#paso-8-viewmodel)
   - [Paso 9. Interfaz con Compose](#paso-9-interfaz-con-compose)
   - [Paso 10. MainActivity](#paso-10-mainactivity)
5. [Testear con una API falsa](#testear-con-una-api-falsa)
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
│ ViewModel: PokemonViewModel                                      │
└──────────────────────────────────┬───────────────────────────────┘
                                   │ getPokemon(id)
┌──────────────────────────────────▼───────────────────────────────┐
│ Model: PokemonRepository ──► ApiService (Retrofit) ──► PokéAPI   │
└──────────────────────────────────────────────────────────────────┘
```

- El **estado baja** (`uiState`) y los **eventos suben** (`onRandomClick`). Es un flujo unidireccional.
- Solo `PokemonScreen` conoce el ViewModel. El resto de composables reciben datos y lambdas, así que se pueden previsualizar sin red.
- Las dependencias se pasan **por constructor**: `ApiService` → `PokemonRepository` → `PokemonViewModel`. Eso permite testear cada capa con piezas falsas.

Estructura de carpetas (`app/src/main/java/com/example/mypokemon/`):

```
MainActivity.kt                 Punto de entrada: tema + PokemonScreen
data/
  remote/ApiService.kt          Interfaz Retrofit (endpoints)
  remote/RetrofitInstance.kt    Configuración de Retrofit (singleton)
  repository/PokemonRepository.kt
model/
  Pokemon.kt, Sprites.kt,       Clases que Gson rellena con el JSON
  Types.kt, Type.kt
  PokemonUiState.kt             Estados de pantalla: Loading / Success / Error
viewmodel/PokemonViewModel.kt   Estado + lógica + Factory
ui/
  screen/                       Pantallas y layout común
  components/                   Imagen, detalles y etiqueta de tipo
  theme/                        Tema, colores y tipografía
  utils/Utils.kt                Clase de tamaño de ventana
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

[libraries]
retrofit = { module = "com.squareup.retrofit2:retrofit", version.ref = "retrofit" }
converter-gson = { module = "com.squareup.retrofit2:converter-gson", version.ref = "converterGson" }
coil-compose = { module = "io.coil-kt.coil3:coil-compose", version.ref = "coilCompose" }
coil-network-okhttp = { module = "io.coil-kt.coil3:coil-network-okhttp", version.ref = "coilCompose" }
androidx-lifecycle-viewmodel-compose = { module = "androidx.lifecycle:lifecycle-viewmodel-compose", version.ref = "lifecycleRuntimeKtx" }
androidx-compose-material3-window-size-class1 = { group = "androidx.compose.material3", name = "material3-window-size-class", version.ref = "material3WindowSizeClass" }
```

Y en `app/build.gradle.kts`, dentro de `dependencies { }`:

```kotlin
implementation(libs.retrofit)                                   // Cliente HTTP declarativo
implementation(libs.converter.gson)                             // JSON ⇄ clases Kotlin
implementation(libs.coil.compose)                               // Carga de imágenes en Compose
implementation(libs.coil.network.okhttp)                        // Descarga de imágenes por HTTP
implementation(libs.androidx.lifecycle.viewmodel.compose)       // viewModel() en Compose
implementation(libs.androidx.compose.material3.window.size.class1) // Layout adaptativo
```

| Librería | Para qué se usa |
|---|---|
| `retrofit` | Convierte una interfaz Kotlin anotada en llamadas HTTP. |
| `converter-gson` | Le dice a Retrofit cómo convertir el JSON de la respuesta en objetos Kotlin. |
| `coil-compose` + `coil-network-okhttp` | Descargan y muestran el sprite del Pokémon. Coil 3 necesita el módulo de red aparte. |
| `lifecycle-viewmodel-compose` | Da la función `viewModel()` para obtener el ViewModel desde un composable. |
| `material3-window-size-class` | Calcula si la pantalla es Compact, Medium o Expanded. |

### Paso 3. Permiso de Internet

Sin este permiso, cualquier llamada de red falla. En `app/src/main/AndroidManifest.xml`, antes de `<application>`:

```xml
<uses-permission android:name="android.permission.INTERNET" />
```

### Paso 4. Modelos de datos (Gson)

Primero hay que ver qué devuelve la API. Esta es una versión reducida de `GET https://pokeapi.co/api/v2/pokemon/25`:

```json
{
  "id": 25,
  "name": "pikachu",
  "height": 4,
  "weight": 60,
  "sprites": { "front_default": "https://.../25.png", "back_default": "..." },
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
)

data class Sprites(@SerializedName("front_default") val frontDefault: String? = null /* … */)
data class Types(@SerializedName("slot") val slot: Int? = null, @SerializedName("type") val type: Type? = Type())
data class Type(@SerializedName("name") val name: String? = null, @SerializedName("url") val url: String? = null)
```

Puntos clave:

- `@SerializedName("front_default")` enlaza la clave del JSON (snake_case) con la propiedad Kotlin (`frontDefault`).
- **Todos los parámetros tienen valor por defecto.** Así Kotlin genera un constructor sin parámetros, Gson lo usa, y las claves que falten en el JSON conservan su valor por defecto.
- **Los campos son nullable.** Gson no respeta la nulabilidad de Kotlin: si el JSON trae `"name": null`, asigna `null` aunque el tipo sea `String`, y la app falla después. Declararlos nullable obliga a la UI a manejar ese caso (`?.`, `?:`).
- La estructura anidada del JSON (`types[].type.name`) se reproduce con clases anidadas (`Types` → `Type`).

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

Aquí se usa `Response<T>` para que el repositorio decida qué hacer con cada respuesta. Si no es correcta, la convierte en `HttpException` (ver [5.4](#54-tipos-de-error-y-dónde-se-manejan)). Además cubre el caso de un 200 sin cuerpo, que con `Pokemon` como retorno no se podría controlar.

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

El repositorio no captura nada: deja que cada error llegue al ViewModel como una excepción de un tipo distinto. El ViewModel tiene un `catch` por tipo y muestra un mensaje en español:

| Situación | Excepción | Mensaje en pantalla |
|---|---|---|
| Sin conexión, timeout, DNS | `IOException` (la lanza OkHttp) | "Sin conexión. Revisa tu red e inténtalo de nuevo." |
| El servidor responde 404, 500… o sin cuerpo | `HttpException` (la lanza `PokemonRepository`) | "El servidor respondió con error (404)." |
| JSON con formato inesperado | `JsonSyntaxException` u otra (la lanza Gson) | "Respuesta inesperada del servidor." |
| La corrutina se cancela | `CancellationException` | Ninguno: se relanza. |

Tres reglas que conviene respetar al replicarlo:

1. **Relanzar `CancellationException`.** Cuando el ViewModel se destruye, `viewModelScope` cancela sus corrutinas lanzando esta excepción. Un `catch (e: Exception)` la atraparía y la corrutina seguiría ejecutándose. Por eso va en el primer `catch`, y se relanza.
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

`data/repository/PokemonRepository.kt` es el intermediario entre el ViewModel y la API. Recibe `ApiService` **por constructor**, en lugar de usar `RetrofitInstance` directamente, para poder sustituirlo por una versión falsa en los tests.

```kotlin
class PokemonRepository(
    private val api: ApiService
) {
    suspend fun getPokemon(idOrName: String): Pokemon {
        val response = api.getPokemon(idOrName)
        return response.body()?.takeIf { response.isSuccessful }
            ?: throw HttpException(response)
    }
}
```

- Si la respuesta es correcta y tiene cuerpo, devuelve el `Pokemon`.
- Si no, lanza `HttpException(response)`, la misma excepción que lanzaría Retrofit. Conserva el código HTTP (`e.code()`), así el ViewModel puede mostrarlo.
- Las excepciones de red y de Gson **no** se capturan aquí. Suben al ViewModel, que decide qué mensaje mostrar.

### Paso 7. Estado de la UI

`model/PokemonUiState.kt` define los tres estados posibles de la pantalla:

```kotlin
sealed class PokemonUiState {
    object Loading : PokemonUiState()
    data class Success(val pokemon: Pokemon) : PokemonUiState()
    data class Error(val message: String) : PokemonUiState()
}
```

Al ser `sealed`, un `when (uiState)` sin los tres casos no compila. Así ningún estado queda sin manejar.

### Paso 8. ViewModel

`viewmodel/PokemonViewModel.kt` guarda el estado y contiene la lógica:

```kotlin
class PokemonViewModel(
    private val repository: PokemonRepository
) : ViewModel() {
    var uiState: PokemonUiState by mutableStateOf(PokemonUiState.Loading)
        private set

    init { getRandomPokemon() }

    fun getRandomPokemon() {
        viewModelScope.launch {
            uiState = PokemonUiState.Loading
            try {
                val randomId = Random.nextInt(1, 1026).toString()
                uiState = PokemonUiState.Success(repository.getPokemon(randomId))
            } catch (e: CancellationException) {
                throw e
            } catch (e: IOException) {
                uiState = PokemonUiState.Error("Sin conexión. Revisa tu red e inténtalo de nuevo.")
            } catch (e: HttpException) {
                uiState = PokemonUiState.Error("El servidor respondió con error (${e.code()}).")
            } catch (e: Exception) {
                uiState = PokemonUiState.Error("Respuesta inesperada del servidor.")
            }
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer { PokemonViewModel(PokemonRepository(RetrofitInstance.api)) }
        }
    }
}
```

- `mutableStateOf` hace que Compose **recomponga** la UI cada vez que cambia `uiState`.
- `private set` impide que la UI modifique el estado. Solo el ViewModel lo cambia.
- `viewModelScope.launch` lanza una corrutina que se cancela sola si el ViewModel se destruye. Aquí se llama a la función `suspend` de Retrofit.
- Los `catch` convierten cada tipo de error en un mensaje para el usuario (ver [5.4](#54-tipos-de-error-y-dónde-se-manejan)).
- **`Factory`**: Android crea los ViewModel con un constructor sin parámetros. Como este recibe un repositorio, la `Factory` le indica cómo construirlo con las dependencias reales.

### Paso 9. Interfaz con Compose

**`ui/screen/PokemonScreen.kt`**: punto de entrada de la UI. Obtiene el ViewModel con la `Factory` y elige el layout según el ancho de la ventana:

```kotlin
@Composable
fun PokemonScreen(
    viewModel: PokemonViewModel = viewModel(factory = PokemonViewModel.Factory)
) {
    Scaffold(topBar = { /* logo + título */ }) { innerPadding ->
        when (getWindowSizeClass().widthSizeClass) {
            WindowWidthSizeClass.Compact -> PokemonCompactScreen(
                uiState = viewModel.uiState,
                onRandomClick = viewModel::getRandomPokemon,
                modifier = Modifier.padding(innerPadding)
            )
            WindowWidthSizeClass.Medium,
            WindowWidthSizeClass.Expanded -> PokemonMediumScreen(/* mismos parámetros */)
        }
    }
}
```

**`ui/screen/PokemonStateLayout.kt`**: layout común. Resuelve el `when (uiState)` (spinner, mensaje de error) y pinta el botón. Cada pantalla solo aporta cómo se ve el estado `Success`:

```kotlin
PokemonStateLayout(uiState = uiState, onRandomClick = onRandomClick, modifier = modifier) { pokemon ->
    // Layout propio de cada pantalla con PokemonImage y PokemonDetails
}
```

- **`PokemonCompactScreen`**: columna con la imagen arriba y los detalles abajo.
- **`PokemonMediumScreen`**: fila con los detalles a la izquierda y la imagen a la derecha. La fila no usa pesos: `Arrangement.spacedBy(48.dp, Alignment.CenterHorizontally)` separa los dos elementos y centra el grupo en la pantalla.

**Componentes (`ui/components/`)**:

- `PokemonImage`: `SubcomposeAsyncImage` de Coil, con spinner mientras carga y el logo como imagen de error.
- `PokemonDetails`: muestra `#id - Nombre`, los tipos, y peso/altura divididos por 10 (PokéAPI usa hectogramos y decímetros).
- `PokemonTypePill`: traduce el tipo al español y lo colorea con `PokemonTypeColors`.

Como las pantallas no conocen el ViewModel, sus `@Preview` usan datos fijos (`previewPokemon`) y se ven sin red.

### Paso 10. MainActivity

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

## Testear con una API falsa

Gracias a la inyección por constructor, el repositorio se puede probar sin red, implementando `ApiService` a mano:

```kotlin
class FakeApiService(private val result: Response<Pokemon>) : ApiService {
    override suspend fun getPokemon(idOrName: String) = result
}

@Test
fun `lanza HttpException con el código si el servidor responde 404`() = runTest {
    val api = FakeApiService(Response.error(404, "".toResponseBody()))
    val error = runCatching { PokemonRepository(api).getPokemon("25") }.exceptionOrNull()
    assertEquals(404, (error as HttpException).code())
}

@Test
fun `devuelve el Pokémon si la respuesta es correcta`() = runTest {
    val api = FakeApiService(Response.success(Pokemon(id = 25, name = "pikachu")))
    assertEquals("pikachu", PokemonRepository(api).getPokemon("25").name)
}
```

Para estos tests hace falta añadir `kotlinx-coroutines-test` (`runTest`). `toResponseBody()` viene de `okhttp3.ResponseBody.Companion`. Para testear el ViewModel, ten en cuenta que usa `viewModelScope` (`Dispatchers.Main`): en tests unitarios hay que reemplazarlo con `Dispatchers.setMain(...)`.

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
