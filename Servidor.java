import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

/*
 * Servidor UDP.
 *
 * UDP no establece una conexion como TCP: el servidor solo abre un
 * DatagramSocket en un puerto y espera paquetes (DatagramPacket).
 * Cada paquete recibido trae la direccion IP y el puerto de quien lo envio,
 * y con esos datos el servidor sabe a donde mandar la respuesta.
 */
public class Servidor {

    static final int PUERTO = 5000;

    public static void main(String[] args) throws IOException {
        // Se abre el socket en el puerto 5000 para escuchar peticiones.
        try (DatagramSocket socket = new DatagramSocket(PUERTO)) {
            System.out.println("Servidor UDP escuchando en el puerto " + PUERTO);

            byte[] buffer = new byte[1024];

            while (true) {
                // 1. Esperar una peticion. receive() se bloquea hasta que llega un paquete.
                DatagramPacket peticion = new DatagramPacket(buffer, buffer.length);
                socket.receive(peticion);

                String mensaje = new String(peticion.getData(), 0, peticion.getLength());
                System.out.println("Peticion de " + peticion.getAddress() + ":" + peticion.getPort()
                        + " -> " + mensaje);

                // 2. Armar la respuesta con todos los tipos de datos.
                byte[] datos = crearRespuesta();

                // 3. Enviar la respuesta a la misma IP y puerto de donde vino la peticion.
                DatagramPacket respuesta = new DatagramPacket(datos, datos.length,
                        peticion.getAddress(), peticion.getPort());
                socket.send(respuesta);

                System.out.println("Respuesta enviada (" + datos.length + " bytes)");
            }
        }
    }

    /*
     * Un datagrama solo transporta bytes. DataOutputStream convierte cada tipo
     * de dato de Java a bytes en un orden fijo; el cliente debe leerlos en
     * ese mismo orden para reconstruirlos.
     */
    static byte[] crearRespuesta() throws IOException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(bytes);

        out.writeByte(127);              // byte    (1 byte)
        out.writeShort(32000);           // short   (2 bytes)
        out.writeInt(1);                 // int     (4 bytes)
        out.writeLong(129039023L);       // long    (8 bytes)
        out.writeFloat(1.23f);           // float   (4 bytes)
        out.writeDouble(12.342);         // double  (8 bytes)
        out.writeChar('C');              // char    (2 bytes)
        out.writeBoolean(true);          // boolean (1 byte)
        out.writeUTF("Hola");            // String  (2 bytes de longitud + texto)

        // Para el arreglo primero se manda su tamaño y despues cada elemento.
        int[] arreglo = {1, 2, 3, 4};
        out.writeInt(arreglo.length);
        for (int n : arreglo) {
            out.writeInt(n);
        }

        out.flush();
        return bytes.toByteArray();
    }
}
