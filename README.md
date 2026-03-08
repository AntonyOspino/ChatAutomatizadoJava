# 📊 Asistente de Análisis de Datos - Frontend (Java)

![Java](https://img.shields.io/badge/Java-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![Maven](https://img.shields.io/badge/Maven-C71A36?style=for-the-badge&logo=apachemaven&logoColor=white)
![FlatLaf](https://img.shields.io/badge/UI-FlatLaf-blue?style=for-the-badge)

Aplicación de escritorio interactiva construida en **Java (Swing)** que actúa como el *frontend* automatizado para un sistema de Análisis Exploratorio de Datos (EDA). El cliente se comunica de forma asíncrona mediante peticiones HTTP con una API REST en Python, guiando al usuario paso a paso a través de una interfaz moderna y amigable.

---

## ✨ Características Principales

* **Interfaz Moderna (Estilo WhatsApp):** Diseño a pantalla completa con esquinas redondeadas, burbujas de chat personalizadas, sombras suaves y soporte nativo para Emojis 🚀 gracias a la librería **FlatLaf**.
* **Comunicación Asíncrona (`SwingWorker`):** La interfaz nunca se congela. Las peticiones a la API y las transiciones de carga se ejecutan en hilos de fondo, brindando una experiencia de usuario fluida.
* **Arquitectura de Máquina de Estados:** El flujo del chat está estrictamente controlado por un `Enum` (`EstadoChat`), garantizando que el usuario siga el proceso lógico del análisis sin saltarse pasos.
* **Validación de Datos:** Bloqueo de campos vacíos y manejo de errores de conexión con alertas visuales.
* **Splash Screen:** Pantalla de carga inicial corporativa antes de lanzar la ventana principal del asistente.

## 🛠️ Tecnologías y Dependencias

El proyecto utiliza **Maven** como gestor de dependencias. Las librerías principales configuradas en el `pom.xml` son:

* **[FlatLaf (3.2.1)](https://www.formdev.com/flatlaf/):** Look and Feel moderno para modernizar los componentes Swing tradicionales.
* **[OkHttp3 (4.11.0)](https://square.github.io/okhttp/):** Cliente HTTP eficiente para realizar las peticiones POST y GET a la API REST.
* **[Gson (2.10.1)](https://github.com/google/gson):** Serialización y deserialización para procesar las respuestas JSON del servidor de Python.

## ⚙️ Requisitos Previos

* **Java Development Kit (JDK):** Versión 17 o superior.
* **Apache Maven:** Para la compilación y gestión de dependencias.
* **API Backend:** La API de Python (FastAPI) debe estar en ejecución localmente (o en la nube) en el puerto `8000`.

## 🚀 Instalación y Ejecución

1. **Clonar el repositorio:**
   ```bash
   git clone [https://github.com/AntonyOspino/ChatAutomatizadoJava.git](https://github.com/AntonyOspino/ChatAutomatizadoJava.git)
   cd ChatAutomatizadoJava
