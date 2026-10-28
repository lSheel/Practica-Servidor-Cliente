import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.util.Arrays;

/*
 * Cliente UDP.
 *
 * Envia un datagrama de peticion al servidor y espera el datagrama de
 * respuesta con los tipos de datos. Como UDP no garantiza la entrega,
 * se usa un tiempo limite para no quedarse esperando para siempre.
 *
 * Uso: java Cliente [ip_del_servidor]   (por defecto localhost)
 */
public class Cliente {

    static final int PUERTO = 5000;   // Debe ser el mismo puerto del servidor.

    public static void main(String[] args) throws IOException {
        String host = args.length > 0 ? args[0] : "localhost";
        InetAddress servidor = InetAddress.getByName(host);

        // Sin puerto: el sistema operativo asigna uno libre al cliente.
        try (DatagramSocket socket = new DatagramSocket()) {
            socket.setSoTimeout(5000);

            // 1. Enviar la peticion al servidor (IP + puerto 5000).
            byte[] mensaje = "DATOS".getBytes();
            DatagramPacket peticion = new DatagramPacket(mensaje, mensaje.length,
                    servidor, PUERTO);
            socket.send(peticion);
            System.out.println("Peticion enviada a " + servidor + ":" + PUERTO);

            // 2. Esperar la respuesta.
            byte[] buffer = new byte[1024];
            DatagramPacket respuesta = new DatagramPacket(buffer, buffer.length);
            socket.receive(respuesta);

            // 3. Leer los bytes en el mismo orden en que el servidor los escribio.
            DataInputStream in = new DataInputStream(
                    new ByteArrayInputStream(respuesta.getData(), 0, respuesta.getLength()));

            System.out.println("Recibo de servidor:");
            System.out.println("byte: " + in.readByte());
            System.out.println("short: " + in.readShort());
            System.out.println("int: " + in.readInt());
            System.out.println("long: " + in.readLong());
            System.out.println("float: " + in.readFloat());
            System.out.println("double: " + in.readDouble());
            System.out.println("char: " + in.readChar());
            System.out.println("boolean: " + in.readBoolean());
            System.out.println("String: " + in.readUTF());

            int[] arreglo = new int[in.readInt()];
            for (int i = 0; i < arreglo.length; i++) {
                arreglo[i] = in.readInt();
            }
            System.out.println("arrayInt: " + Arrays.toString(arreglo));

        } catch (SocketTimeoutException e) {
            System.out.println("El servidor no respondio en 5 segundos. Verifica que este encendido.");
        }
    }
}
