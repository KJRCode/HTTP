
import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class HTTPClient {

    public static final int PORT = 80;
    public static String SERVER_ADDR = "" ; // "www.google.com";
    //public static final String SERVER_ADDR = "www.google.com" ; // "www.google.com";
    public static final String CRLF = "\r\n";
    public static final String EOH = CRLF + CRLF; //indicates end of header

    public static final int CHUNK_SIZE = 512;				// size of fragment to process


    public static void main(String[] args) {

        SERVER_ADDR = args[0];
        System.out.println("client is requesting ... ");
        try {
            Socket socket = new Socket(SERVER_ADDR, PORT);
            DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
            DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());

//            for(int i = 0; i < args.length; i++){
//                System.out.println(args[i]);
//            }

//            // generate a HTTP request a print writer, handy to handle output stream
//            PrintWriter printWriter = new PrintWriter(new OutputStreamWriter(dataOutputStream, StandardCharsets.ISO_8859_1));
//            printWriter.print("GET / HTTP/1.1" + CRLF);
//            printWriter.print("Host: " + SERVER_ADDR + CRLF);
//            printWriter.print("Connection: close" + CRLF);
//            printWriter.print("Accept: */*" + EOH);
//            printWriter.flush();

            PrintWriter printWriter = new PrintWriter(new OutputStreamWriter(dataOutputStream, StandardCharsets.ISO_8859_1));

            //if the client requests a specific file or path
            if (args.length > 1){
                String want = args[1];
                String fileItself = want;

                //get the name of the file without the subdirectory if given a path name
                if (want.contains("\\")){
                    Scanner scanW = new Scanner(want);
                    fileItself = "";

                    Boolean theName = false;
                    for (int i = 0; i < want.length(); i++){
                        if (theName){
                            fileItself += want.charAt(i);
                        }
                        if (want.charAt(i) == '\\'){
                            theName = true;
                        }
                    }

                }

                // generate an HTTP request a print writer, handy to handle output stream
                printWriter.print("GET " + want + " HTTP/1.1" + CRLF);
                printWriter.print("Host: " + SERVER_ADDR + CRLF);
                printWriter.print("Connection: close" + CRLF);
                printWriter.print("Accept: */*" + EOH);
                printWriter.flush();

                System.out.println("After sending the request, wait for response: ");
                int receiveFileLen = 0;
                Boolean notFound = false;

                // good to handle strings from stream
                BufferedReader bufferedReader = new BufferedReader( new InputStreamReader(dataInputStream));
                String line = bufferedReader.readLine();

                //find out of the response is the 404 error
                if (line.contains("Not Found")){
                    notFound = true;
                    System.out.println(line);
                }

                while(line != null && !line.isEmpty()){
                    line  = bufferedReader.readLine();

                    //if the response is the 404 error then we want to print the response
                    if(notFound){
                        System.out.println(line);
                    }

                    //grab the content length
                    if (line.contains("Content-Length: ")){
                        Scanner scanLine = new Scanner(line);
                        scanLine.useDelimiter(" ");
                        String noUse = scanLine.next();
                        receiveFileLen = scanLine.nextInt();
                    }
                }

                if (notFound){

                    bufferedReader.close();
                }
                else{
                    //get the file from HTTPServer
                    byte[] recievedBytes = new byte[receiveFileLen];
                    int NofBytesRead1 = 0;
                    while(NofBytesRead1 != receiveFileLen){
                        //I have no idea why this error keeps happening
                        //I think it's just a one-off error
                        //so this is my temporary fix
                        if (NofBytesRead1 == -1){
                            break;
                        }
                        int readBytes = dataInputStream.read(recievedBytes, NofBytesRead1, receiveFileLen - NofBytesRead1);
                        NofBytesRead1 += readBytes;
                    }

                    //send file's data to client folder
                    File getFile = new File("client_folder\\" + fileItself);
                    FileOutputStream outputStream = new FileOutputStream(getFile);

                    int NofBytesReadOut = 0;
                    while(NofBytesReadOut != receiveFileLen){
                        //I have no idea why this error keeps happening
                        //I think it's just a one-off error
                        //so this is my temporary fix
                        if (NofBytesReadOut == -1){
                            break;
                        }
                        outputStream.write(recievedBytes, NofBytesReadOut, receiveFileLen - NofBytesReadOut);
                        NofBytesReadOut += receiveFileLen - NofBytesReadOut;
                    }


                    System.out.println("Saved: " + getFile.getName());


                    bufferedReader.close();
                }

            }



            //if the client only provides the IP address
            else{
                // generate an HTTP request a print writer, handy to handle output stream
                printWriter.print("GET / HTTP/1.1" + CRLF);
                printWriter.print("Host: " + SERVER_ADDR + CRLF);
                printWriter.print("Connection: close" + CRLF);
                printWriter.print("Accept: */*" + CRLF);
                printWriter.print(CRLF);
                printWriter.flush();

                System.out.println("After sending the request, wait for response: ");
                int receiveFileLen = 0;

                // good to handle strings from stream
                //read the incoming HTTP response and use it to get the content length
                BufferedReader bufferedReader = new BufferedReader( new InputStreamReader(dataInputStream));
                String line = bufferedReader.readLine();
                System.out.println(line);

                while(line != null && !line.isEmpty()){
                    line  = bufferedReader.readLine();
                    System.out.println(line);

                    if (line.contains("Content-Length: ")){

                        Scanner scanLine = new Scanner(line);
                        scanLine.useDelimiter(" ");

                        String noUse = scanLine.next();
                        receiveFileLen = scanLine.nextInt();

                        scanLine.close();
                    }
                }


                //get the file from HTTPServer
                byte[] recievedBytes = new byte[receiveFileLen];
                int NofBytesRead1 = 0;
                while(NofBytesRead1 != receiveFileLen){
                //I have no idea why this error keeps happening
                // I think it's just a one-off error
                //so this is my temporary fix
                    if (NofBytesRead1 == -1){
                        break;
                    }
                    int readBytes = dataInputStream.read(recievedBytes, NofBytesRead1, receiveFileLen - NofBytesRead1);
                    NofBytesRead1 += readBytes;
                }

                //send data to client folder

                File getFile = new File("client_folder\\" + "index.html");
                FileOutputStream outputStream = new FileOutputStream(getFile);
                outputStream.write(recievedBytes, 0, receiveFileLen);

                System.out.println("Saved: " + "index.html");


                bufferedReader.close();

            }


            printWriter.close();
            dataInputStream.close();
            dataOutputStream.close();


        }catch (UnknownHostException e){
            e.printStackTrace();
        }catch(IOException e){
            e.printStackTrace();
        }
    }

}
