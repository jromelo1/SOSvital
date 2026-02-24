# SOSQuake Bogotá 🛡️🌋
**Sistema Móvil de Resiliencia Sísmica y Red de Auxilio Ciudadana**

SOSQuake es una solución de ingeniería de software diseñada para la respuesta inmediata post-sismo en Bogotá, Colombia. A diferencia de las aplicaciones convencionales, SOSQuake transforma el dispositivo móvil en una baliza de rescate autónoma capaz de operar incluso ante el colapso de las redes de datos (4G/5G).

## 🚀 Características Principales

* **Detección e Intervención Crítica:** Interfaz de pánico que se sobrepone a la pantalla de bloqueo con cuenta regresiva de 30 segundos.
* **Protocolo de Auxilio Multicanal:**
    * **Visual:** Señalización S.O.S. mediante flash LED estroboscópico.
    * **Sonora:** Alarma de alta frecuencia que ignora el modo "No molestar" y fuerza el volumen al 100%.
    * **Mensajería Persistente:** Envío de SMS con coordenadas GPS mediante un sistema de reintentos inteligentes hasta confirmar la entrega.
* **Gestión de Energía Extrema:** Algoritmos de supervivencia que ajustan los ciclos de trabajo (Duty Cycles) según el nivel de batería restante.
* **Arquitectura Mesh (Prototipo):** Preparado para la transmisión de paquetes de ayuda vía Bluetooth Low Energy (BLE) en escenarios sin señal celular.

## 🛠️ Stack Tecnológico

* **Lenguaje:** Kotlin 1.9+
* **UI:** Jetpack Compose (Modern Material Design 3)
* **Arquitectura:** MVVM con Servicios de Primer Plano (Foreground Services)
* **Localización:** Google Play Services Location (Fused Location Provider)
* **Concurrencia:** Kotlin Coroutines para procesos de fondo no bloqueantes
* **Persistencia:** SharedPreferences (Configuración) y Room (Mesh Relay)

## 📋 Requisitos de Instalación

1.  Clonar el repositorio: `git clone https://github.com/tu-usuario/sosquake-bogota.git`
2.  Abrir en **Android Studio Jellyfish** o superior.
3.  Sincronizar Gradle y compilar en un dispositivo físico (recomendado para pruebas de sensores).

## 🧪 Pruebas de Estrés (QA)

Para garantizar la fiabilidad del sistema, el proyecto ha sido sometido a:
1.  **Test de Jaula de Faraday:** Validación de persistencia de SMS ante pérdida total de señal.
2.  **Sobreescritura de Audio:** Confirmación de prioridad del stream de alarma sobre aplicaciones multimedia.
3.  **Simulación de Acelerómetro:** Pruebas de estabilidad de la UI ante eventos sísmicos simulados.

## 📍 Contexto Bogotá
Este proyecto toma en cuenta la densidad poblacional de Bogotá y la topografía de la ciudad, optimizando el uso de GPS y Bluetooth para mejorar la probabilidad de rescate en zonas de alta edificación.

---
**Desarrollado con enfoque en Resiliencia Urbana.**
