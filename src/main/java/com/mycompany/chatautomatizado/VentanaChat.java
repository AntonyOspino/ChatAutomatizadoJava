package com.mycompany.chatautomatizado;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
// --- NUEVOS IMPORTS PARA LA API ---
import okhttp3.*;
import com.google.gson.*;
import java.io.IOException;
/**
 *
 * @author ALEJO
 */

public class VentanaChat extends JFrame {
    private JTextArea areaMensajes;
    private JTextField campoTexto;
    private JButton btnEnviar;
    private EstadoChat estadoActual = EstadoChat.PEDIR_NOMBRE;
    private String nombreUsuario = "";
    
    // --- NUEVAS VARIABLES PARA LA CONEXIÓN ---
    private OkHttpClient clienteHttp = new OkHttpClient();
    private Gson gson = new Gson();
    private final String URL_API = "http://127.0.0.1:8000";
    
    // Aquí guardaremos los IDs que nos devuelva Python
    private int sesionId = -1;
    private int datasetId = -1;
    private int informeId = -1;
    private String tipoArchivoElegido = "";
    
    // --- NUEVAS VARIABLES PARA LAS COLUMNAS ---
    private String colCuantitativasElegidas = "";
    private String colCualitativasElegidas = "";

    public VentanaChat() {
        // Configuración de la ventana
        setExtendedState(JFrame.MAXIMIZED_BOTH); // Pantalla completa
        setTitle("Asistente de Análisis de Datos");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        // Configurar los componentes
        areaMensajes = new JTextArea();
        areaMensajes.setEditable(false);
        areaMensajes.setLineWrap(true);
        areaMensajes.setWrapStyleWord(true);
        areaMensajes.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 16));
        areaMensajes.setMargin(new Insets(10, 10, 10, 10)); // Márgenes internos
        
        campoTexto = new JTextField();
        campoTexto.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        btnEnviar = new JButton("Enviar");
        btnEnviar.setFont(new Font("Segoe UI", Font.BOLD, 14));
        
        // Acomodar todo en la pantalla
        JPanel panelInferior = new JPanel(new BorderLayout(10, 10));
        panelInferior.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panelInferior.add(campoTexto, BorderLayout.CENTER);
        panelInferior.add(btnEnviar, BorderLayout.EAST);
        
        add(new JScrollPane(areaMensajes), BorderLayout.CENTER);
        add(panelInferior, BorderLayout.SOUTH);

        // Darle vida al botón y al Enter
        btnEnviar.addActionListener(this::procesarMensaje);
        campoTexto.addActionListener(this::procesarMensaje);

        // Iniciar el chat
        agregarMensajeAsistente("¡Hola! ¿Cómo estás? ¿Cómo te llamas?");
    }

    private void agregarMensajeAsistente(String mensaje) {
    areaMensajes.append("🤖 Asistente: " + mensaje + "\n\n");
    areaMensajes.setCaretPosition(areaMensajes.getDocument().getLength());
    }

    private void agregarMensajeUsuario(String mensaje) {
        areaMensajes.append("👤 Tú: " + mensaje + "\n\n");
    }

    private void procesarMensaje(ActionEvent e) {
        String input = campoTexto.getText().trim();
        
        // VALIDACIÓN DEL PROFESOR: Campo vacío
        if (input.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No puede dejar campos vacíos.", "Atención", JOptionPane.WARNING_MESSAGE);
            return;
        }

        agregarMensajeUsuario(input);
        campoTexto.setText("");

        // Lógica de la Máquina de Estados
        switch (estadoActual) {
            case PEDIR_NOMBRE:
                nombreUsuario = input;
                // En vez de avanzar directamente, llamamos a la API
                crearSesionEnApi(nombreUsuario);
                break;
                
            case PREGUNTAR_ANALISIS:
                if (input.equalsIgnoreCase("no")) {
                    cerrarAplicacion("Gracias por usar nuestros servicios");
                } else if (input.equalsIgnoreCase("si")) {
                    agregarMensajeAsistente("1.- Datos medianos o pequeños con Pandas con CSV\n2.- Datos medianos o pequeños con Pandas con XLSX");
                    estadoActual = EstadoChat.PREGUNTAR_TIPO_ARCHIVO;
                } else {
                    agregarMensajeAsistente("Por favor, responda solo 'Si' o 'No'.");
                }
                break;

            case PREGUNTAR_TIPO_ARCHIVO:
                if (input.equals("1")) {
                    tipoArchivoElegido = "csv";
                    agregarMensajeAsistente("Copie el link de la data");
                    estadoActual = EstadoChat.PEDIR_URL_DATA;
                } else if (input.equals("2")) {
                    tipoArchivoElegido = "xlsx";
                    agregarMensajeAsistente("Copie el link de la data");
                    estadoActual = EstadoChat.PEDIR_URL_DATA;
                } else {
                    agregarMensajeAsistente("Por favor, seleccione la opción 1 o 2.");
                }
                break;

            case PEDIR_URL_DATA:
                // Llamamos a la API enviando el enlace que puso el usuario
                cargarDatosEnApi(input);
                break;
                
            case PEDIR_COLUMNAS_CUANTITATIVAS:
                colCuantitativasElegidas = input;
                agregarMensajeAsistente("Indique sus columnas CUALITATIVAS (escríbalas tal cual, separadas por coma):");
                estadoActual = EstadoChat.PEDIR_COLUMNAS_CUALITATIVAS;
                break;
                
            case PEDIR_COLUMNAS_CUALITATIVAS:
                colCualitativasElegidas = input;
                // Llamamos a la API para que haga el trabajo pesado
                generarAnalisisYPdfEnApi();
                break;
                
            case PEDIR_CORREO:
                // Llamamos a la API para enviar el email con el PDF adjunto
                enviarCorreoEnApi(input);
                break;
                
            case FINALIZADO:
                cerrarAplicacion("Gracias por usar nuestros servicios señor " + nombreUsuario);
                break;
        }
    }
    
    // --- MÉTODO PARA CONECTAR CON /sesiones/crear ---
    private void crearSesionEnApi(String nombreCompleto) {
        campoTexto.setEnabled(false);
        btnEnviar.setEnabled(false);
        agregarMensajeAsistente("Conectando con el servidor para iniciar tu sesión...");

        SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                // 1. Dividir el nombre y el apellido (como lo pide la API)
                String[] partes = nombreCompleto.split(" ", 2);
                String nombre = partes[0];
                String apellido = (partes.length > 1) ? partes[1] : "";

                // 2. Crear el JSON para enviar: {"nombre": "...", "apellido": "..."}
                JsonObject jsonEnvio = new JsonObject();
                jsonEnvio.addProperty("nombre", nombre);
                jsonEnvio.addProperty("apellido", apellido);

                // 3. Configurar la petición HTTP POST
                RequestBody body = RequestBody.create(jsonEnvio.toString(), MediaType.parse("application/json; charset=utf-8"));
                Request request = new Request.Builder()
                        .url(URL_API + "/sesiones/crear")
                        .post(body)
                        .build();

                // 4. Ejecutar la petición
                try (Response response = clienteHttp.newCall(request).execute()) {
                    if (response.isSuccessful() && response.body() != null) {
                        // Leer la respuesta de Python y guardar el sesion_id
                        String respuestaString = response.body().string();
                        JsonObject jsonRespuesta = JsonParser.parseString(respuestaString).getAsJsonObject();
                        sesionId = jsonRespuesta.get("sesion_id").getAsInt();
                        return true;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return false;
            }

            @Override
            protected void done() {
                campoTexto.setEnabled(true);
                btnEnviar.setEnabled(true);
                campoTexto.requestFocus();

                try {
                    boolean exito = get();
                    if (exito) {
                        agregarMensajeAsistente("¡Sesión iniciada con éxito! (ID: " + sesionId + ")\n¿Deseas realizar un análisis exploratorio de datos? Si o no");
                        estadoActual = EstadoChat.PREGUNTAR_ANALISIS;
                    } else {
                        agregarMensajeAsistente("Hubo un error al conectar con el servidor de Python. Asegúrate de que esté encendido.");
                        estadoActual = EstadoChat.PEDIR_NOMBRE; // Volver a intentar
                    }
                } catch (Exception e) {
                    agregarMensajeAsistente("Error interno al procesar la conexión.");
                }
            }
        };
        worker.execute();
    }
    
    // --- MÉTODO PARA CONECTAR CON /datos/cargar y /datos/columnas ---
    private void cargarDatosEnApi(String urlData) {
        campoTexto.setEnabled(false);
        btnEnviar.setEnabled(false);
        agregarMensajeAsistente("Descargando y analizando los datos en el servidor... Un momento por favor ⏳");

        SwingWorker<String, Void> worker = new SwingWorker<>() {
            @Override
            protected String doInBackground() throws Exception {
                try {
                    // 1. PRIMERA LLAMADA: Enviar la URL a /datos/cargar
                    JsonObject jsonCargar = new JsonObject();
                    jsonCargar.addProperty("url", urlData);
                    jsonCargar.addProperty("tipo", tipoArchivoElegido);
                    jsonCargar.addProperty("sesion_id", sesionId);

                    RequestBody bodyCargar = RequestBody.create(jsonCargar.toString(), MediaType.parse("application/json; charset=utf-8"));
                    Request requestCargar = new Request.Builder()
                            .url(URL_API + "/datos/cargar")
                            .post(bodyCargar)
                            .build();

                    try (Response resCargar = clienteHttp.newCall(requestCargar).execute()) {
                        if (!resCargar.isSuccessful() || resCargar.body() == null) {
                            return "Error: No se pudo cargar el archivo. Revisa que el enlace sea correcto.";
                        }
                        // Guardar el dataset_id que nos devuelve Python
                        String stringCargar = resCargar.body().string();
                        datasetId = JsonParser.parseString(stringCargar).getAsJsonObject().get("dataset_id").getAsInt();
                    }

                    // 2. SEGUNDA LLAMADA: Pedir las columnas a /datos/columnas
                    Request requestColumnas = new Request.Builder()
                            .url(URL_API + "/datos/columnas")
                            .get()
                            .build();

                    try (Response resCols = clienteHttp.newCall(requestColumnas).execute()) {
                        if (!resCols.isSuccessful() || resCols.body() == null) {
                            return "Error: No se pudieron obtener las columnas del archivo.";
                        }
                        
                        // Extraer las listas de columnas del JSON
                        String stringCols = resCols.body().string();
                        JsonObject jsonCols = JsonParser.parseString(stringCols).getAsJsonObject();
                        JsonArray colCuantitativas = jsonCols.getAsJsonArray("cuantitativas");
                        JsonArray colCualitativas = jsonCols.getAsJsonArray("cualitativas");

                        // Armar el mensaje bonito para el usuario
                        return "¡Conjunto de datos cargados exitosamente! ✅\n\n" +
                               "📊 Columnas Cuantitativas detectadas:\n" + colCuantitativas.toString() + "\n\n" +
                               "📝 Columnas Cualitativas detectadas:\n" + colCualitativas.toString() + "\n\n" +
                               "Por favor, indique sus columnas CUANTITATIVAS (escríbalas tal cual aparecen, separadas por coma):";
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    return "Error de conexión con la API de Python.";
                }
            }

            @Override
            protected void done() {
                campoTexto.setEnabled(true);
                btnEnviar.setEnabled(true);
                campoTexto.requestFocus();

                try {
                    String respuesta = get();
                    agregarMensajeAsistente(respuesta);
                    
                    if (!respuesta.startsWith("Error")) {
                        // Si todo salió bien, avanzamos al siguiente estado
                        estadoActual = EstadoChat.PEDIR_COLUMNAS_CUANTITATIVAS;
                    }
                } catch (Exception e) {
                    agregarMensajeAsistente("Ocurrió un error inesperado al mostrar los datos.");
                }
            }
        };
        worker.execute();
    }
    
    // --- MÉTODO PARA CONECTAR CON /analisis/ejecutar y /pdf/generar ---
    private void generarAnalisisYPdfEnApi() {
        campoTexto.setEnabled(false);
        btnEnviar.setEnabled(false);
        agregarMensajeAsistente("Generando análisis exploratorio y creando el informe en PDF... Esto puede tardar unos minutos, por favor espera. ⏳");

        SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                try {
                    // 1. Convertir el texto "col1, col2" en un arreglo JSON ["col1", "col2"]
                    JsonArray arrayCuant = new JsonArray();
                    for (String col : colCuantitativasElegidas.split(",")) {
                        if (!col.trim().isEmpty()) arrayCuant.add(col.trim());
                    }

                    JsonArray arrayCual = new JsonArray();
                    for (String col : colCualitativasElegidas.split(",")) {
                        if (!col.trim().isEmpty()) arrayCual.add(col.trim());
                    }

                    // 2. PRIMERA LLAMADA: Ejecutar el análisis y crear gráficos
                    JsonObject jsonAnalisis = new JsonObject();
                    jsonAnalisis.addProperty("dataset_id", datasetId);
                    jsonAnalisis.add("columnas_cuantitativas", arrayCuant);
                    jsonAnalisis.add("columnas_cualitativas", arrayCual);

                    RequestBody bodyAnalisis = RequestBody.create(jsonAnalisis.toString(), MediaType.parse("application/json; charset=utf-8"));
                    Request reqAnalisis = new Request.Builder()
                            .url(URL_API + "/analisis/ejecutar")
                            .post(bodyAnalisis)
                            .build();

                    try (Response resAnalisis = clienteHttp.newCall(reqAnalisis).execute()) {
                        if (!resAnalisis.isSuccessful()) {
                            System.err.println("Error en Análisis: " + resAnalisis.body().string());
                            return false;
                        }
                    }

                    // 3. SEGUNDA LLAMADA: Generar el archivo PDF
                    JsonObject jsonPdf = new JsonObject();
                    jsonPdf.addProperty("dataset_id", datasetId);
                    // Por defecto no enviamos "incluir_outliers" como dice el README

                    RequestBody bodyPdf = RequestBody.create(jsonPdf.toString(), MediaType.parse("application/json; charset=utf-8"));
                    Request reqPdf = new Request.Builder()
                            .url(URL_API + "/pdf/generar")
                            .post(bodyPdf)
                            .build();

                    try (Response resPdf = clienteHttp.newCall(reqPdf).execute()) {
                        if (!resPdf.isSuccessful() || resPdf.body() == null) {
                            return false;
                        }
                        
                        // Extraemos el ID del informe para usarlo en el envío por correo luego
                        String stringPdf = resPdf.body().string();
                        informeId = JsonParser.parseString(stringPdf).getAsJsonObject().get("informe_id").getAsInt();
                        return true;
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    return false;
                }
            }

            @Override
            protected void done() {
                campoTexto.setEnabled(true);
                btnEnviar.setEnabled(true);
                campoTexto.requestFocus();

                try {
                    if (get()) {
                        agregarMensajeAsistente("¡Informe generado exitosamente! 📄✅\nIndique su correo electrónico, señor(a) " + nombreUsuario + ":");
                        estadoActual = EstadoChat.PEDIR_CORREO;
                    } else {
                        agregarMensajeAsistente("Ocurrió un error al generar el análisis. Verifica que las columnas estén escritas correctamente e intenta de nuevo.");
                        estadoActual = EstadoChat.PEDIR_COLUMNAS_CUANTITATIVAS; // Volvemos a pedir para no dañar el flujo
                    }
                } catch (Exception e) {
                    agregarMensajeAsistente("Error interno crítico al generar el informe.");
                }
            }
        };
        worker.execute();
    }
    
    // --- MÉTODO PARA CONECTAR CON /correo/enviar ---
    private void enviarCorreoEnApi(String correoUsuario) {
        campoTexto.setEnabled(false);
        btnEnviar.setEnabled(false);
        agregarMensajeAsistente("Un momento por favor, se está enviando el informe a su correo... 📧");

        SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                try {
                    // Armamos el JSON con los datos que pide el README
                    JsonObject jsonCorreo = new JsonObject();
                    jsonCorreo.addProperty("informe_id", informeId);
                    jsonCorreo.addProperty("correo", correoUsuario);
                    jsonCorreo.addProperty("sesion_id", sesionId);

                    RequestBody bodyCorreo = RequestBody.create(jsonCorreo.toString(), MediaType.parse("application/json; charset=utf-8"));
                    Request reqCorreo = new Request.Builder()
                            .url(URL_API + "/correo/enviar")
                            .post(bodyCorreo)
                            .build();

                    // Ejecutamos la petición
                    try (Response resCorreo = clienteHttp.newCall(reqCorreo).execute()) {
                        return resCorreo.isSuccessful(); // Retorna true si Python envió el correo bien
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    return false;
                }
            }

            @Override
            protected void done() {
                try {
                    boolean exito = get();
                    if (exito) {
                        estadoActual = EstadoChat.FINALIZADO;
                        // Si todo salió bien, llamamos al método que cierra la app
                        cerrarAplicacion("¡Informe enviado! 📬\nMuchas gracias por usar nuestros servicios señor(a) " + nombreUsuario);
                    } else {
                        campoTexto.setEnabled(true);
                        btnEnviar.setEnabled(true);
                        campoTexto.requestFocus();
                        agregarMensajeAsistente("Hubo un error al enviar el correo desde el servidor. Por favor, verifica la dirección e intenta de nuevo:");
                    }
                } catch (Exception e) {
                    agregarMensajeAsistente("Error interno al intentar enviar el correo.");
                }
            }
        };
        worker.execute();
    }

    private void bloquearInterfazYSimularCarga(String mensajeFinal, EstadoChat siguienteEstado) {
        campoTexto.setEnabled(false);
        btnEnviar.setEnabled(false);
        
        if(estadoActual == EstadoChat.PEDIR_COLUMNAS_CUALITATIVAS){
             agregarMensajeAsistente("Generando análs exploratorio en un informe en PDF. Espere unos minutos por favor...");
        } else if (estadoActual == EstadoChat.PEDIR_CORREO){
             agregarMensajeAsistente("Un momento por favor, se esta enviando el informe a su correo...");
        } else {
             agregarMensajeAsistente("Un momento por favor...");
        }

        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                Thread.sleep(3000); // Simulamos la carga de 3 segundos
                return null;
            }

            @Override
            protected void done() {
                campoTexto.setEnabled(true);
                btnEnviar.setEnabled(true);
                campoTexto.requestFocus();
                agregarMensajeAsistente(mensajeFinal);
                estadoActual = siguienteEstado;
            }
        };
        worker.execute();
    }

    private void cerrarAplicacion(String mensajeDespedida) {
        campoTexto.setEnabled(false);
        btnEnviar.setEnabled(false);
        agregarMensajeAsistente(mensajeDespedida);
        
        // Espera 3 segundos y cierra el programa (Requisito del profesor)
        Timer timer = new Timer(3000, e -> System.exit(0));
        timer.setRepeats(false);
        timer.start();
    }
}