# Tragaperras Android · Producto 1

Aplicación **Android nativa**, escrita en **Java y XML**, correspondiente al producto 1 de Aplicaciones móviles nativas. Es un juego de azar con tres rodillos animados, monedas virtuales e histórico local. Los comentarios explican en español la responsabilidad de las clases y sus funciones.

El proyecto se abre directamente en Android Studio. La primera versión permite empezar una partida, girar los rodillos, consultar las jugadas y recuperar el saldo guardado. La interfaz, las reglas y el acceso a los datos se organizan en capas para facilitar su comprensión y sus modificaciones. La carpeta `docs/` contiene el análisis, el mockup, los diagramas y la memoria.

## 1. Qué incluye

- Bienvenida con iniciar o continuar, nueva partida, cerrar aplicación y saldo guardado.
- Pantalla de juego con tres rodillos que se desplazan verticalmente y frenan de forma escalonada.
- Botones Girar, Parar y Volver al inicio.
- Barra de acción con Inicio, Histórico de jugadas y Cómo se juega.
- Cinco dibujos vectoriales del proyecto, guardados como recursos estáticos.
- Saldo, sesiones y jugadas almacenados en SQLite dentro del dispositivo.
- Consultas y escrituras asíncronas con RxJava; actualización de la interfaz con RxAndroid.
- Tres mejores jugadas con fecha en la bienvenida y un histórico completo.
- Pruebas unitarias de las reglas y de la coordinación del giro.

El alcance de esta versión es el **producto 1**: un juego local con monedas virtuales y persistencia en SQLite. Las funciones multimedia y de integración del producto 2 no forman parte de esta entrega.

## 2. Reglas del juego

Estas cifras son decisiones de diseño y se pueden modificar en `GameRules`; el enunciado no fija las cantidades.

| Concepto | Valor |
| --- | --- |
| Saldo al empezar una partida | 100 monedas |
| Coste de cada giro | 10 monedas |
| Símbolos posibles por rodillo | 5, todos con la misma probabilidad |
| Tres iguales | Premio bruto de 100; cambio neto de +90 |
| Exactamente dos iguales | Premio bruto de 10; cambio neto de 0 |
| Tres distintos | Sin premio; cambio neto de −10 |

Los tres símbolos se sortean al pulsar **Girar**. **Parar** adelanta la frenada; no permite elegir el resultado por habilidad. Si no se pulsa, los rodillos comienzan a detenerse automáticamente. El saldo nunca puede quedar por debajo de cero.

**Nueva partida** devuelve el saldo a 100, cierra la sesión anterior y conserva el histórico. En este proyecto, una *partida* es una sesión que contiene varias *jugadas* o giros. Las mejores jugadas se ordenan por ganancia neta; en caso de empate, aparece primero la más reciente.

## 3. Abrirlo en Android Studio

### Herramientas y versiones

| Herramienta | Configuración del proyecto |
| --- | --- |
| IDE | Android Studio compatible con Android Gradle Plugin 8.13 |
| Lenguaje y compilador | Java / JDK 17 |
| Android Gradle Plugin | 8.13.2 |
| Gradle | 8.13, incluido mediante Gradle Wrapper |
| SDK de compilación y destino | API 35, Android 15 |
| SDK mínimo de instalación | API 24, Android 7.0 |
| SDK Build-Tools | 35.0.0, versión predeterminada de esta configuración |
| Bibliotecas | RxJava 3.1.12 y RxAndroid 3.0.2 |
| Pruebas locales | JUnit 4.13.2 |

Android Studio es el programa en el que editas y ejecutas el proyecto. El **SDK** aporta las herramientas y las plataformas Android que utiliza para compilarlo. Gradle organiza esa compilación y descarga las dependencias.

1. Instala [Android Studio](https://developer.android.com/studio?hl=es-419) y completa su asistente inicial si todavía no lo tienes.
2. Descomprime este archivo ZIP en una carpeta de tu ordenador.
3. En Android Studio, selecciona **Open / Abrir** y elige la carpeta **TragaperrasAndroid**, que contiene `settings.gradle`. No selecciones solo `app` o `src` y no crees otro proyecto vacío encima.
4. En **SDK Manager**, instala la plataforma **Android 15 / API 35**. En las herramientas del SDK, comprueba que estén instalados **Android SDK Build-Tools 35.0.0** y **Android SDK Platform-Tools**. Instala también **Android Emulator** si utilizarás un emulador. Acepta las licencias que muestre Android Studio.
5. Deja terminar la sincronización de Gradle. La primera apertura necesita Internet para descargar Gradle y las bibliotecas; el juego funciona sin conexión una vez instalado.
6. Si aparece un error de Java, revisa **Settings / Preferences → Build, Execution, Deployment → Build Tools → Gradle → Gradle JDK**. Para reproducir esta base, selecciona o descarga un **JDK 17**. Es un JDK completo, no solamente un JRE.
7. Selecciona la configuración **app**, el dispositivo y pulsa **Run ▶**.

Los nombres de algunos menús pueden variar según la versión o el idioma de Android Studio. El IDE creará `local.properties` con la ubicación de tu SDK. Ese archivo no se comparte, porque la ruta cambia en cada ordenador.

### Ejecutar en un emulador

Abre **Device Manager**, crea un dispositivo virtual de tipo teléfono y descarga una imagen de sistema. Para la primera comprobación se propone una imagen **API 35**. Inicia el emulador, selecciónalo en la barra de Android Studio y ejecuta `app`.

### Ejecutar en un móvil

Activa las opciones de desarrollador y la depuración USB en un teléfono con Android 7.0 o superior. Conéctalo por USB y acepta en el teléfono la autorización del ordenador. Cuando Android Studio lo detecte, selecciónalo como destino de ejecución. Algunos fabricantes requieren un controlador USB en Windows.

## 4. Organización: MVP y capas

Se utiliza **MVP: Modelo, Vista y Presentador**. La vista representa información y comunica pulsaciones. El presentador decide qué acciones están permitidas y solicita datos. Las reglas calculan los resultados; el repositorio se ocupa de guardarlos.

Todas las rutas de Java de esta tabla parten de `app/src/main/java/com/example/tragaperras/`.

| Carpeta o archivo | Responsabilidad |
| --- | --- |
| `domain/GameRules.java` | Sorteos, coste, premios y cálculo del saldo. Java independiente de Android. |
| `domain/SpinResult.java` | Resultado inmutable de una jugada. |
| `domain/PlayerSnapshot.java` y `HomeData.java` | Datos del jugador y de la bienvenida. |
| `domain/GameRepository.java` | Contrato de operaciones de datos, expresado con `Single` de RxJava. |
| `data/GameDatabase.java` | Creación de tablas SQLite y versión de la base de datos. |
| `data/SqliteGameRepository.java` | Consultas, transacciones y ejecución en segundo plano. |
| `presentation/HomePresenter.java` | Carga de bienvenida y comienzo de una sesión. |
| `presentation/GamePresenter.java` | Estados del giro, bloqueo de botones y coordinación de la animación. |
| `presentation/HistoryPresenter.java` | Carga del histórico. |
| `ui/MainActivity.java`, `GameActivity.java`, `HistoryActivity.java` | Conectan el XML con los presentadores y el ciclo de vida Android. |
| `ui/ReelView.java` | Dibuja y anima los rodillos con Canvas y ValueAnimator. No calcula premios. |
| `ui/BaseActivity.java` | Barra de acción, navegación, márgenes del sistema y avisos. |
| `ui/SpinAdapter.java` y `SymbolArt.java` | Presentación de filas y correspondencia entre símbolos y dibujos. |
| `SlotApplication.java` | Crea una única instancia del repositorio para la aplicación. |
| `app/src/main/res/` | Diseños XML, dibujos, colores, estilos, textos y menú. |
| `app/src/test/` | Pruebas ejecutables sin emulador. |

Las reglas y los modelos no importan clases Android. Los presentadores dependen del contrato del repositorio y reciben un planificador RxJava por constructor, lo que permite probarlos sin pantallas reales. Las Activity no contienen consultas SQL ni cálculos de premios.

### ¿Dónde cambio cada cosa?

| Quiero modificar… | Archivo o carpeta |
| --- | --- |
| Distribución de la bienvenida | `app/src/main/res/layout/activity_main.xml` |
| Distribución de la máquina y sus botones | `app/src/main/res/layout/activity_game.xml` |
| Histórico y sus filas | `app/src/main/res/layout/activity_history.xml` y `row_spin.xml` |
| Textos y nombre de la aplicación | `app/src/main/res/values/strings.xml` |
| Colores, tipografía y botones | `app/src/main/res/values/colors.xml`, `styles.xml` y `res/color/` |
| Dibujos de los cinco símbolos | `app/src/main/res/drawable/symbol_*.xml` |
| Tamaño y aspecto del rodillo | `res/values/dimens.xml`, `res/values-land/dimens.xml` y `ui/ReelView.java` |
| Movimiento y frenada | `ui/ReelView.java`; tiempos de inicio de frenada en `ui/GameActivity.java` |
| Monedas iniciales, coste y premios | Constantes de `domain/GameRules.java` |
| Qué ocurre al pulsar un botón | El presentador de la pantalla correspondiente |
| Datos que se guardan | `data/GameDatabase.java` y `data/SqliteGameRepository.java` |

Si cambias el **número de símbolos**, actualiza conjuntamente `GameRules.SYMBOL_COUNT`, los recursos de `SymbolArt`, los nombres de `symbol_names` y las pruebas. Mantén su orden: cerezas, manzana, campana, estrella, gema. Si solo sustituyes los dibujos conservando los cinco identificadores, no necesitas cambiar los premios.

## 5. Guardado, RxJava y ciclo de vida

La base `tragaperras.db` contiene tres tablas:

| Tabla | Datos principales |
| --- | --- |
| `player` | Un único jugador, saldo y sesión activa. |
| `sessions` | Inicio, cierre y saldo inicial de cada partida. |
| `spins` | Sesión, fecha, tres símbolos, coste, premio y saldo anterior y posterior. |

`SqliteGameRepository` envuelve cada operación en `Single.fromCallable()` y utiliza un planificador con un único hilo de datos. Así se serializan las operaciones y SQLite no bloquea el hilo de la interfaz. Los presentadores reciben el resultado en el hilo principal mediante `observeOn()` y el planificador de RxAndroid.

El resultado y el saldo final se guardan **juntos en una transacción, antes de iniciar la animación**. Si falla la escritura, no empieza el movimiento y se muestra un aviso. Mientras los rodillos giran, la pantalla enseña el saldo tras descontar el coste; al terminar, revela el premio y el saldo final ya guardado.

Si la aplicación pasa a segundo plano o cambia de orientación, se cancelan sus animaciones y observadores visuales. Al volver, se carga el estado persistido. Si una transacción alcanzó a confirmarse, la jugada aparece resuelta, sin repetir la apuesta. La animación no se reanuda desde el fotograma anterior. Si el proceso se interrumpe antes de confirmar una transacción, SQLite conserva el estado previo.

Los botones y el menú se bloquean durante el guardado y el giro para evitar solicitudes repetidas. Cerrar la aplicación conserva sus datos; desinstalarla o borrar sus datos elimina el histórico local. Esta base no activa copias de seguridad automáticas.

## 6. Comprobación del proyecto

Comprobación realizada el **2 de octubre de 2026**, con JDK 17 y Android SDK 35:

- **Compilación de depuración completada**: se ha generado el APK correctamente.
- **14 pruebas unitarias superadas**, sin fallos ni pruebas omitidas: 8 de reglas y modelos, y 6 del presentador del juego.
- **Android Lint: 0 errores y 16 avisos**. Los avisos se refieren a usar SDK 35 cuando existe uno posterior, atributos limitados a versiones recientes, pluralización de textos, configuración de copia de seguridad, fondos superpuestos y concatenación de un mensaje. Se mantienen visibles; no se han desactivado las comprobaciones.

El **1 de noviembre de 2026** se confirmó una primera ejecución y una prueba de juego satisfactoria en el entorno Android configurado. Esta confirmación general no sustituye el registro de cada caso: siguen pendientes las comprobaciones detalladas de persistencia tras cerrar, interrupciones, accesibilidad y adaptación visual. La lista siguiente permite completarlas.

Las pruebas cubren las 125 combinaciones posibles, el saldo insuficiente, el desbordamiento, la inmutabilidad del resultado, las pulsaciones repetidas, la orden de parar, los fallos de guardado y la desconexión y reconexión de la vista.

Desde la carpeta raíz del proyecto, en **Windows / PowerShell**:

```powershell
# Compila el APK de depuración, ejecuta pruebas y analiza el código Android.
.\gradlew.bat :app:assembleDebug :app:testDebugUnitTest :app:lintDebug
```

En **macOS o Linux**:

```bash
# Habilita el lanzador si el descompresor no conservó su permiso de ejecución.
chmod +x gradlew
# Compila el APK de depuración, ejecuta pruebas y analiza el código Android.
./gradlew :app:assembleDebug :app:testDebugUnitTest :app:lintDebug
```

El APK se genera en `app/build/outputs/apk/debug/app-debug.apk`. Es una compilación de desarrollo, firmada automáticamente para pruebas. Los informes locales aparecen en `app/build/reports/`.

### Comprobaciones manuales en Android

1. Abrir por primera vez y crear una partida: debe aparecer el saldo de 100.
2. Girar y comprobar el desplazamiento vertical y la detención automática de los tres rodillos.
3. Girar de nuevo y pulsar Parar: deben frenar de forma escalonada y mostrar el resultado guardado.
4. Pulsar Girar repetidamente: cada giro aceptado debe generar una sola fila en el histórico.
5. Volver al inicio, consultar el histórico, cerrar y reabrir: saldo y resultados deben mantenerse.
6. Crear una nueva partida: el saldo vuelve a 100 y el histórico anterior sigue disponible.
7. Rotar el dispositivo y enviarlo a segundo plano durante un giro: al volver debe aparecer un estado coherente, sin un segundo cargo por la misma jugada.
8. Revisar la pantalla en vertical y horizontal, con letra ampliada y en un teléfono pequeño; se puede desplazar el contenido para acceder a los botones.
9. Comprobar el estado sin monedas suficientes: Girar debe quedar desactivado y debe poderse volver al inicio.

## 7. Documentación y entrega del producto 1

| Archivo o carpeta | Contenido |
| --- | --- |
| `docs/Memoria_Producto1.docx` | Memoria editable de análisis, diseño, trabajo realizado, dificultades, mejoras y autoevaluación. |
| `docs/Memoria_Producto1.pdf` | Copia de lectura de la memoria. |
| `docs/Requisitos_funcionales.md` | Requisitos y criterios para comprobarlos. |
| `docs/mockup/` | Mockup de las tres pantallas en SVG y PNG. |
| `docs/uml/` | Diagramas de clases y fuente Mermaid editable. |
| `docs/Registro_pruebas.md` | Resultados disponibles y casos manuales por completar. |
| `docs/Guia_entrega.md` | Preparación del repositorio, el vídeo y el ZIP del aula. |
| `docs/entrega/` | Documentos para incorporar los enlaces reales de GitHub y del vídeo. |

Antes de entregar: completar las pruebas manuales, revisar la memoria y su autoevaluación, publicar el código en el repositorio indicado por el centro y grabar la demostración. Los documentos de enlaces están pendientes de completar; no hay enlaces publicados en esta versión.

Esta revisión actualiza comentarios y documentación. Mantiene la versión de la app **0.1** y el esquema SQLite **1**. No requiere desinstalar la aplicación ni borrar sus datos. Si cambia el esquema en otra revisión, será necesario implementar una migración.

## 8. Convenciones para continuar

- Mantener comentarios en español en las clases y funciones propias: qué hacen, qué reciben y las decisiones que no resulten evidentes.
- Mantener los textos en `strings.xml` y los estilos en recursos, en vez de dispersarlos por Java.
- Mantener los sorteos y premios fuera de las Activity y de `ReelView`.
- Mantener las operaciones SQLite dentro del repositorio y fuera del hilo principal.
- Actualizar las pruebas y este README cuando cambien las reglas o la configuración.
- No compartir `local.properties`, claves de firma, cachés ni carpetas de compilación. `.gitignore` ya excluye esos archivos habituales.

Los lanzadores de Gradle son los archivos oficiales de Gradle, sin traducir ni modificar su lógica. Su licencia se incluye en `GRADLE-LICENSE.txt`; los comentarios en español se aplican al código propio del proyecto.

## 9. Recursos y documentación

- [Android Studio](https://developer.android.com/studio?hl=es-419).
- [Crear un proyecto Android](https://developer.android.com/studio/projects/create-project).
- [Administrar dispositivos virtuales](https://developer.android.com/studio/run/managing-avds).
- [Ejecutar en un dispositivo físico](https://developer.android.com/studio/run/device).
- [Dibujo de vistas personalizadas con Canvas](https://developer.android.com/develop/ui/views/layout/custom-views/custom-drawing).
- [Animaciones de propiedades y ValueAnimator](https://developer.android.com/develop/ui/views/animations/prop-animation).
- [Guardar datos con SQLite](https://developer.android.com/training/data-storage/sqlite).
- [Android Gradle Plugin 8.13 y compatibilidad](https://developer.android.com/build/releases/agp-8-13-0-release-notes).
- [RxJava](https://github.com/ReactiveX/RxJava) y [RxAndroid](https://github.com/ReactiveX/RxAndroid).
- [Cursos de Android facilitados](https://developer.android.com/courses?hl=es-419).
- [Curso O’Reilly facilitado](https://learning.oreilly.com/course/android-app-developer/9780134664071/).
- [Recursos de Firebase facilitados](https://firebase.google.com/community/learn?hl=es-419), como material complementario del curso.

Versión de la aplicación: **0.1**. Documentación revisada el **1 de noviembre de 2026**.
