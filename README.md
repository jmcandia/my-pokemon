# My Pokémon

App Android que muestra un Pokémon aleatorio (ID 1–1025) obtenido de [PokéAPI](https://pokeapi.co/). Cada vez que se pulsa **¡Buscar Pokémon aleatorio!** pide uno nuevo; también se puede buscar uno por nombre o número. Muestra su imagen, número, nombre, tipos, peso, altura, habilidades y estadísticas base, y permite escuchar su grito.

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
  remote/RetrofitInstance.kt    Configuración de Retrofit y OkHttp (con caché HTTP)
  remote/OfflineCacheInterceptor.kt  Sin red, responde con la copia en caché
  repository/
    PokemonRepository.kt        Contrato que usa el ViewModel
    DefaultPokemonRepository.kt Implementación con Retrofit
    PokemonException.kt         Errores del repositorio: Network / NotFound / Server / Unknown
model/
  Pokemon.kt, Sprites.kt,       DTOs: clases que Gson rellena con el JSON
  OtherSprites.kt, OfficialArtwork.kt,
  Types.kt, Type.kt,
  PokemonStat.kt, PokemonAbility.kt, Cries.kt, NamedResource.kt
viewmodel/
  PokemonViewModel.kt           Estado + lógica + Factory
  PokemonUiState.kt             Estados de pantalla: Loading / Success / Error
  PokemonUi.kt                  Modelo listo para mostrar + conversión desde el DTO
  PokemonSearch.kt              Normaliza lo que escribe el usuario para PokéAPI
ui/
  screen/                       Pantallas y layout común
  components/                   Búsqueda, imagen, detalles, tipo, estadísticas y grito
  theme/                        Tema, colores y tipografía
  utils/Utils.kt                Clase de tamaño de ventana
  utils/UiText.kt               Texto sin resolver (recurso + argumentos)
res/values/strings.xml          Todos los textos de la app (español, idioma por defecto)
res/values-en/strings.xml       Traducción al inglés
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
    @SerializedName("types") val types: List<Types>? = null
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

// Estadísticas, habilidades y grito (campos `stats`, `abilities` y `cries` de Pokemon).
data class PokemonStat(@SerializedName("base_stat") val baseStat: Int? = null, @SerializedName("stat") val stat: NamedResource? = null)
data class PokemonAbility(@SerializedName("ability") val ability: NamedResource? = null, @SerializedName("is_hidden") val isHidden: Boolean? = null)
data class Cries(@SerializedName("latest") val latest: String? = null)  // URL de un .ogg
data class NamedResource(@SerializedName("name") val name: String? = null, @SerializedName("url") val url: String? = null)
```

Puntos clave:

- `@SerializedName("front_default")` enlaza la clave del JSON (snake_case) con la propiedad Kotlin (`frontDefault`).
- **Todos los parámetros tienen valor por defecto.** Así Kotlin genera un constructor sin parámetros, Gson lo usa, y las claves que falten en el JSON conservan su valor por defecto.
- **Los campos son nullable.** Gson no respeta la nulabilidad de Kotlin: si el JSON trae `"name": null`, asigna `null` aunque el tipo sea `String`, y la app falla después. Vale también para las listas: `"types": null` dejaría en `null` un `List<Types>` no nullable. Declararlos nullable obliga a manejar ese caso (`?.`, `?:`, `orEmpty()`), cosa que hace `toUi()` en el [paso 7](#paso-7-estado-de-la-ui).
- La estructura anidada del JSON (`types[].type.name`) se reproduce con clases anidadas (`Types` → `Type`).
- `NamedResource` representa el patrón `{"name": …, "url": …}` que PokéAPI usa para enlazar recursos (`stats[].stat`, `abilities[].ability`). `name` es un identificador en inglés con guiones (`special-attack`, `lightning-rod`).
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
    private const val CACHE_SIZE_BYTES = 10L * 1024 * 1024

    fun createApi(cacheDir: File): ApiService {
        val client = OkHttpClient.Builder()
            .cache(Cache(File(cacheDir, "http"), CACHE_SIZE_BYTES))  // 1. Caché HTTP en disco
            .addInterceptor(OfflineCacheInterceptor())               // 2. Caché sin conexión
            .build()

        return Retrofit.Builder()
            .baseUrl(BASE_URL)                                   // 3. URL base
            .client(client)                                      // 4. Cliente OkHttp propio
            .addConverterFactory(GsonConverterFactory.create())  // 5. JSON ⇄ Kotlin con Gson
            .build()                                             // 6. Crea el cliente Retrofit
            .create(ApiService::class.java)                      // 7. Implementa la interfaz
    }
}
```

1. **`cache`**: OkHttp guarda las respuestas en disco y respeta sus cabeceras. PokéAPI envía `Cache-Control: max-age=86400`, así que un Pokémon ya visto se reutiliza durante un día sin descargarlo otra vez.
2. **`OfflineCacheInterceptor`**: si falla la red, repite la petición con `only-if-cached` y acepta copias caducadas de hasta 30 días. Si no hay copia, relanza el `IOException` original, para que el usuario vea "Sin conexión".
3. **`baseUrl`** debe terminar en `/`. Retrofit lanza `IllegalArgumentException` si no termina así. Las rutas de `@GET` **no** empiezan con `/`: una ruta con `/` inicial se resuelve desde la raíz del dominio y pierde el `api/v2/`.
4. **`client`**: sin esta línea, Retrofit crea un `OkHttpClient` por defecto, sin caché.
5. **`addConverterFactory`** registra Gson. Sin un converter, Retrofit no sabe crear un `Pokemon` a partir del JSON.
6. **`build()`** crea el objeto `Retrofit`.
7. **`create(ApiService::class.java)`** genera un objeto que implementa `ApiService`. Al llamar a `api.getPokemon("25")`, Retrofit arma la URL, hace la petición y convierte la respuesta.

Crear el cliente es costoso, así que la app debe hacerlo **una sola vez**. `createApi` recibe la carpeta de caché, que solo se conoce con un `Context`, así que no puede ser un `val` de un `object`. La llama `AppContainer` una vez, de forma perezosa (`by lazy`), y guarda el resultado (ver [paso 10](#paso-10-application-y-mainactivity)).

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
| No existe ese nombre o número | Respuesta 404 | `PokemonException.NotFound` | "No existe ningún Pokémon «agumon»." |
| El servidor responde 500… o sin cuerpo | Respuesta no exitosa | `PokemonException.Server(code)` | "El servidor respondió con error (500)." |
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

#### 5.6. R8 en release

Gson crea los DTOs y rellena sus campos por reflexión. Si R8 renombra los campos o borra el constructor vacío del paquete `model`, el JSON deja de mapearse y los campos quedan en `null`. Por eso `app/src/main/keepRules/rules.keep` incluye:

```
-keep class com.example.mypokemon.model.** { *; }
```

Hoy `packageScope` (en `app/build.gradle.kts`) limita R8 a `androidx`, `kotlin` y `kotlinx`, así que el código de la app no se optimiza. La regla protege los DTOs si ese alcance se amplía.

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
    class NotFound : PokemonException()
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
        if (response.code() == HTTP_NOT_FOUND) throw PokemonException.NotFound()
        return response.body()?.takeIf { response.isSuccessful }
            ?: throw PokemonException.Server(response.code())
    }
}
```

- Si la respuesta es correcta y tiene cuerpo, devuelve el `Pokemon`.
- Si responde 404, lanza `PokemonException.NotFound`: el nombre o número buscado no existe, y el ViewModel muestra un mensaje propio.
- Con cualquier otro error, lanza `PokemonException.Server` con el código HTTP, para que el ViewModel pueda mostrarlo.
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

Todos los textos (botón, errores, título, tipos) están en `res/values/strings.xml`, en español, que es el idioma por defecto. `res/values-en/strings.xml` tiene la traducción al inglés: Android la elige sola si el dispositivo está en inglés. Para otro idioma basta con añadir otra carpeta (`values-pt`, `values-fr`…). `app_name` está marcado con `translatable="false"` porque es un nombre propio.

**`viewmodel/PokemonUi.kt`: el Pokémon listo para mostrar.** El DTO `Pokemon` tiene campos nullable y unidades de PokéAPI. `toUi()` lo convierte en un modelo que la View pinta tal cual:

```kotlin
data class PokemonUi(
    val title: UiText,          // "#25 - Pikachu" o "Pokémon desconocido"
    val imageUrl: String?,      // artwork oficial o, si falta, el sprite
    val types: List<PokemonTypeUi>,
    val weight: UiText,         // R.string.weight_value con 6.0, o "—"
    val height: UiText,         // R.string.height_value con 0.4, o "—"
    val stats: List<PokemonStatUi>,   // PS, Ataque… con valor y proporción de la barra
    val abilities: List<UiText>,      // "Static", "Lightning rod (oculta)"
    val cryUrl: String?               // URL del grito (.ogg)
)

data class PokemonTypeUi(val key: String, val label: UiText)  // "electric", "Eléctrico"
data class PokemonStatUi(val label: UiText, val value: Int, val fraction: Float)  // "Ataque", 55, 0.22
```

- `formatTenths` divide por 10 (PokéAPI usa hectogramos y decímetros) y devuelve el recurso `%1$.1f kg` con ese número. Si falta un dato devuelve `—`.
- **¿Por qué `UiText` y no un `String` ya formateado?** El separador decimal depende del idioma (`6,0 kg` en español, `6.0 kg` en inglés). Android lo aplica al resolver el recurso en la View, con el idioma vigente. Si el ViewModel guardara el texto ya formateado, sobreviviría a un cambio de idioma con el separador antiguo.
- `formatTitle` maneja la falta de número o de nombre. El nombre pasa por `formatName`: PokéAPI une palabras y formas con guiones (`mr-mime`, `toxtricity-amped`), que se muestran como "Mr Mime" y "Toxtricity Amped".
- `typeUi` traduce el tipo con su recurso (`R.string.type_electric`). Un tipo desconocido queda capitalizado. `key` es el nombre en inglés; la View lo usa para elegir el color.
- `statUi` traduce la estadística (`R.string.stat_attack`) y calcula `fraction = valor / 255`, la estadística base más alta que existe, así las barras son comparables entre Pokémon. Una estadística sin valor se omite.
- `abilityUi` convierte el identificador en texto (`lightning-rod` → "Lightning rod") y marca las ocultas con "(oculta)". Las habilidades quedan en inglés: PokéAPI solo da su traducción en `/ability/{id}`, que sería una petición más por habilidad.

**`viewmodel/PokemonUiState.kt`: los tres estados de la pantalla.**

```kotlin
sealed class PokemonUiState {
    data class Loading(val previous: PokemonUi? = null) : PokemonUiState()
    data class Success(val pokemon: PokemonUi) : PokemonUiState()
    data class Error(val message: UiText) : PokemonUiState()
}
```

Al ser `sealed`, un `when (uiState)` sin los tres casos no compila. Así ningún estado queda sin manejar.

`Loading.previous` es el Pokémon que se mostraba antes de pedir otro. La View lo mantiene atenuado bajo el spinner, así la pantalla no salta en cada búsqueda. En la primera carga, o después de un error, es `null`.

### Paso 8. ViewModel

`viewmodel/PokemonViewModel.kt` guarda el estado y contiene la lógica:

```kotlin
class PokemonViewModel(
    private val repository: PokemonRepository,
    private val savedStateHandle: SavedStateHandle,
    private val random: Random = Random.Default
) : ViewModel() {
    private val _uiState = MutableStateFlow<PokemonUiState>(PokemonUiState.Loading())
    val uiState: StateFlow<PokemonUiState> = _uiState.asStateFlow()

    private var loadJob: Job? = null

    init {
        val savedId: Int? = savedStateHandle[KEY_LAST_ID]
        loadPokemon((savedId ?: randomId()).toString())
    }

    fun getRandomPokemon() { loadPokemon(randomId().toString()) }

    fun searchPokemon(query: String) {
        val idOrName = normalizeQuery(query)   // "Mr. Mime" → "mr-mime", "025" → "25"
        if (idOrName.isEmpty()) return
        loadPokemon(idOrName)
    }

    private fun randomId(): Int = random.nextInt(1, 1026)

    private fun loadPokemon(idOrName: String) {
        if (loadJob?.isActive == true) return

        val previous = (_uiState.value as? PokemonUiState.Success)?.pokemon

        loadJob = viewModelScope.launch {
            _uiState.value = PokemonUiState.Loading(previous)
            _uiState.value = try {
                val pokemon = repository.getPokemon(idOrName)
                pokemon.id?.let { savedStateHandle[KEY_LAST_ID] = it }
                PokemonUiState.Success(pokemon.toUi())
            } catch (e: PokemonException) {
                PokemonUiState.Error(e.toMessage(idOrName))
            }
        }
    }

    private fun PokemonException.toMessage(idOrName: String): UiText = when (this) {
        is PokemonException.Network -> UiText.Resource(R.string.error_network)
        is PokemonException.NotFound -> UiText.Resource(R.string.error_not_found, listOf(idOrName))
        is PokemonException.Server -> UiText.Resource(R.string.error_server, listOf(code))
        is PokemonException.Unknown -> UiText.Resource(R.string.error_unknown)
    }

    companion object {
        private const val KEY_LAST_ID = "last_pokemon_id"

        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = this[APPLICATION_KEY] as PokemonApplication
                PokemonViewModel(
                    repository = application.container.pokemonRepository,
                    savedStateHandle = createSavedStateHandle()
                )
            }
        }
    }
}
```

- **`StateFlow`**: el estado se guarda en un `MutableStateFlow` privado y se expone como `StateFlow` de solo lectura. Así solo el ViewModel lo cambia, y el ViewModel no depende de Compose: en los tests basta con leer `uiState.value`.
- **`loadJob`**: guarda la petición en curso. Si llega otra llamada antes de que termine, se ignora. La UI además desactiva el botón mientras carga.
- **`random`**: también se inyecta. La app usa `Random.Default`; un test pasa `Random(42)` y sabe de antemano qué IDs se pedirán, porque dos `Random` con la misma semilla dan la misma secuencia.
- **`previous`**: antes de cargar, guarda el Pokémon que se está mostrando y lo pasa a `Loading`, para que la View lo mantenga en pantalla.
- **`SavedStateHandle`**: el `ViewModel` sobrevive a una rotación, pero no a que Android cierre la app en segundo plano para liberar memoria. `SavedStateHandle` sí sobrevive: guarda el ID del último Pokémon cargado y, al volver, `init` pide ese mismo en lugar de uno aleatorio. Solo se guarda un ID que cargó bien, y se toma de la respuesta (`pokemon.id`), así también funciona si se buscó por nombre. Sirve para datos pequeños como un ID, no para el Pokémon completo.
- **`searchPokemon`**: recibe el texto tal como lo escribió el usuario. `normalizeQuery` (`viewmodel/PokemonSearch.kt`) lo convierte al formato de PokéAPI: minúsculas, sin tildes, sin puntos ni apóstrofos, guiones en vez de espacios y sin ceros a la izquierda (PokéAPI responde 404 a `025`). Un texto vacío no lanza ninguna petición. Comparte `loadPokemon` con la carga aleatoria, así hereda el bloqueo de peticiones simultáneas y el `Loading(previous)`.
- `viewModelScope.launch` lanza una corrutina que se cancela sola si el ViewModel se destruye. Aquí se llama a la función `suspend` del repositorio.
- **`toUi()`**: convierte el DTO en un `PokemonUi` listo para mostrar. La View nunca ve el `Pokemon` de Gson.
- **Un solo `catch`**: el repositorio solo lanza `PokemonException`, y `toMessage()` elige el recurso de `strings.xml` para cada tipo. Al ser `sealed`, el `when` no compila si se añade un tipo nuevo sin mensaje. El ViewModel no importa nada de Retrofit (ver [5.4](#54-tipos-de-error-y-dónde-se-manejan)).
- **`Factory`**: Android crea los ViewModel con un constructor sin parámetros. Como este recibe un repositorio, la `Factory` le indica cómo construirlo. No crea dependencias: las pide al `AppContainer` de la app (`APPLICATION_KEY` da acceso a la `Application`). `createSavedStateHandle()` le da el `SavedStateHandle` que conserva Android. En los tests se le pasa un `SavedStateHandle()` vacío.

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

**`ui/screen/PokemonStateLayout.kt`**: layout común. Pinta la barra de búsqueda arriba, resuelve el `when (uiState)` y pinta el botón aleatorio. La búsqueda y el botón se desactivan mientras el estado es `Loading`:

- **Búsqueda** (`PokemonSearchBar`): campo de texto y botón "Buscar". El texto que se escribe es estado de la UI (`rememberSaveable`, sobrevive a la rotación); solo se envía con `onSearch` al pulsar "Buscar" o la tecla de búsqueda del teclado, que además se oculta. Normalizarlo es tarea del ViewModel.
- **Carga** sin Pokémon previo: solo el spinner. Con Pokémon previo: el contenido anterior atenuado (`alpha(0.4f)`) y el spinner encima. Carga y éxito usan el mismo contenedor, así la pantalla no cambia de tamaño al terminar.
- **Error**: el mensaje en rojo, con `liveRegion` para que los lectores de pantalla lo anuncien en cuanto aparece.
- La columna tiene `verticalScroll`, para que en pantallas bajas (teléfono en horizontal) el botón siga siendo alcanzable.

Cada pantalla solo aporta cómo se ve un Pokémon cargado:

```kotlin
PokemonStateLayout(uiState, onRandomClick, onSearch, modifier) { pokemon ->
    // Layout propio de cada pantalla con PokemonImage y PokemonDetails
}
```

- **`PokemonCompactScreen`**: columna con la imagen arriba y los detalles abajo.
- **`PokemonMediumScreen`**: fila con los detalles a la izquierda y la imagen a la derecha, y las estadísticas debajo de la fila. La fila no usa pesos: `Arrangement.spacedBy(48.dp, Alignment.CenterHorizontally)` separa los dos elementos y centra el grupo en la pantalla. Las estadísticas quedan fuera (`PokemonDetails(showStats = false)`) porque la imagen se centra en vertical respecto a los detalles: con ellas dentro, la columna sería mucho más alta que la imagen, y la imagen quedaría abajo, cortada en un teléfono en horizontal.

**Componentes (`ui/components/`)**:

- `PokemonImage`: `SubcomposeAsyncImage` de Coil, con spinner mientras carga y el logo como imagen de error. Recibe `pokemon.imageUrl`: la ilustración oficial o, si no existe, el sprite.
- `PokemonDetails`: recibe un `PokemonUi` y muestra el título, el botón del grito, los tipos, el peso y la altura, las habilidades y las estadísticas. No formatea nada: solo resuelve los textos con `asString()` y `stringResource()`. El título lleva `semantics { heading() }`, para que los lectores de pantalla puedan saltar directo a él.
- `PokemonStats`: una fila por estadística con nombre, valor y `LinearProgressIndicator`. Cada fila se lee como un solo elemento ("Ataque 55"), y la barra se oculta a los lectores de pantalla (`clearAndSetSemantics {}`) para que no anuncien un porcentaje confuso.
- `PokemonCryButton`: reproduce el `.ogg` con `MediaPlayer`. `prepareAsync()` lo descarga sin bloquear la UI y `start()` se llama al terminar. Un `DisposableEffect(url)` libera el reproductor al cambiar de Pokémon o salir de la pantalla. Reproducir audio es un efecto de la UI, como cargar la imagen con Coil, así que no pasa por el ViewModel. El grito no usa la caché de OkHttp.
- `PokemonTypePill`: recibe un `PokemonTypeUi`. Muestra el nombre ya traducido y elige el color con `PokemonTypeColors` según `key`. El texto es blanco o negro según el fondo: `readableTextColor` elige el que tenga más contraste con la fórmula de WCAG, así todos los tipos superan 4.5:1.

El logo de la barra superior tiene `contentDescription = null`: es decorativo, porque el título de al lado ya dice el nombre de la app.

Como las pantallas no conocen el ViewModel, sus `@Preview` usan datos fijos (`previewPokemon`, un `Pokemon` convertido con `toUi()`) y se ven sin red.

### Paso 10. Application y MainActivity

**`data/AppContainer.kt`**: el único sitio que decide qué implementación se usa para cada dependencia (inyección manual, sin Hilt ni Koin):

```kotlin
interface AppContainer {
    val pokemonRepository: PokemonRepository
}

class DefaultAppContainer(private val context: Context) : AppContainer {
    override val pokemonRepository: PokemonRepository by lazy {
        DefaultPokemonRepository(RetrofitInstance.createApi(context.cacheDir))
    }
}
```

Recibe el `Context` de la aplicación para ubicar la caché HTTP en la carpeta de caché de la app, que Android vacía si se queda sin espacio. `by lazy` garantiza que el cliente se crea una sola vez.

**`PokemonApplication.kt`**: crea el contenedor una sola vez al arrancar la app. Se registra en el manifest con `android:name` (ver [paso 3](#paso-3-manifest-permiso-de-internet-y-application)):

```kotlin
class PokemonApplication : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = DefaultAppContainer(this)
    }
}
```

Si mañana se añade una base de datos, solo cambia `DefaultAppContainer`. El ViewModel y su `Factory` no se tocan. Así se añadió la caché HTTP: solo cambiaron `RetrofitInstance` y el contenedor.

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

Gracias a la inyección por constructor, cada capa se prueba sin red y con una pieza falsa de la capa de abajo: el repositorio con un `ApiService` falso, y el ViewModel con un repositorio falso. Los tests unitarios están en `app/src/test/java/com/example/mypokemon/` y se ejecutan con `./gradlew testDebugUnitTest`, sin emulador. Los de UI se explican en [Tests de UI](#tests-de-ui).

| Archivo | Qué prueba |
|---|---|
| `FakeApiService.kt` | No es un test: es un `ApiService` falso para los tests del repositorio. Cada test decide qué responde y puede revisar los IDs pedidos. |
| `FakePokemonRepository.kt` | No es un test: es un `PokemonRepository` falso para los tests del ViewModel. Devuelve un `Pokemon` o lanza una `PokemonException`. |
| `MainDispatcherRule.kt` | Regla de JUnit que reemplaza `Dispatchers.Main`, que no existe fuera de Android y que `viewModelScope` necesita. |
| `data/remote/OfflineCacheInterceptorTest.kt` | Con red pasa la respuesta; sin red usa la caché (`only-if-cached`, hasta 30 días); sin red ni copia relanza el `IOException` original. Usa un `Interceptor.Chain` falso. |
| `data/repository/PokemonRepositoryTest.kt` | Respuesta correcta, y la traducción de cada fallo: 404 a `NotFound`, 500 y 200 sin cuerpo a `Server`, error de red a `Network`, JSON ilegible a `Unknown`. |
| `viewmodel/PokemonViewModelTest.kt` | Paso de `Loading` a `Success`, ID entre 1 y 1025, IDs exactos con un `Random` con semilla, Pokémon anterior durante la carga, ID recuperado de `SavedStateHandle`, búsqueda normalizada, búsqueda vacía ignorada, mensaje de "no encontrado" con lo buscado, mensaje para cada `PokemonException`, peticiones simultáneas ignoradas y reintento. |
| `viewmodel/PokemonSearchTest.kt` | `normalizeQuery`: minúsculas, ceros a la izquierda, tildes, puntos, apóstrofos, espacios y texto vacío. |
| `viewmodel/PokemonUiTest.kt` | Conversión a `PokemonUi`: peso y altura divididos por 10, datos faltantes, nulls explícitos de Gson, título, traducción de tipos y estadísticas, barra respecto a 255, habilidades ocultas, y lectura de `stats`/`abilities`/`cries` con las claves reales del JSON (`base_stat`, `is_hidden`). |
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
    val viewModel = PokemonViewModel(FakePokemonRepository { pikachu }, SavedStateHandle())

    assertEquals(PokemonUiState.Loading(), viewModel.uiState.value)
    advanceUntilIdle()
    assertEquals(PokemonUiState.Success(pikachu.toUi()), viewModel.uiState.value)
}
```

Para escribir estos tests hace falta la dependencia `kotlinx-coroutines-test` (`runTest`, `advanceUntilIdle`). `toResponseBody()` viene de `okhttp3.ResponseBody.Companion`; OkHttp ya llega como dependencia de Retrofit.

### Tests de UI

`app/src/androidTest/java/com/example/mypokemon/ui/screen/PokemonScreensTest.kt` prueba las pantallas en un emulador o dispositivo con `./gradlew connectedDebugAndroidTest`. Como las pantallas no conocen el ViewModel, cada test les pasa un estado fijo y comprueba qué muestran, sin red ni repositorio:

| Estado | Qué comprueba |
|---|---|
| `Success` | Título, tipo traducido, peso y botón activo. El título es un encabezado (`isHeading()`). Botón del grito, habilidades y estadísticas (cada fila se lee con nombre y valor juntos). |
| Búsqueda | Envía el texto escrito tal cual con `onSearch`. "Buscar" se desactiva sin texto y durante la carga. |
| `Loading()` | Solo el spinner, sin Pokémon, y el botón desactivado. |
| `Loading(previous)` | El Pokémon anterior sigue visible, con spinner, y el botón desactivado. |
| `Error` | Mensaje visible, anunciado con `liveRegion`, y el botón llama a `onRandomClick`. |
| Pantalla mediana | Muestra el Pokémon y el botón. |

```kotlin
@get:Rule
val composeRule = createComposeRule()

@Test
fun primera_carga_muestra_solo_spinner_y_desactiva_el_boton() {
    composeRule.setContent {
        MyPokémonTheme { PokemonCompactScreen(uiState = PokemonUiState.Loading(), onRandomClick = {}) }
    }

    composeRule.onNode(hasProgressBarRangeInfo(ProgressBarRangeInfo.Indeterminate)).assertIsDisplayed()
    composeRule.onNodeWithText(context.getString(R.string.random_button)).assertIsNotEnabled()
}
```

Los textos se leen con `context.getString(...)` en lugar de escribirlos a mano, así los tests pasan con el dispositivo en español o en inglés.

---

## Comandos útiles

En PowerShell usa `.\gradlew.bat` en lugar de `./gradlew`.

| Comando | Qué hace |
|---|---|
| `./gradlew assembleDebug` | Compila el APK de debug. |
| `./gradlew installDebug` | Instala la app en el emulador o dispositivo conectado. |
| `./gradlew testDebugUnitTest` | Ejecuta los tests unitarios. |
| `./gradlew connectedDebugAndroidTest` | Ejecuta los tests de UI (requiere emulador o dispositivo). |
| `./gradlew lint` | Ejecuta el análisis de Android Lint. |
