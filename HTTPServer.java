
import jdk.jfr.ContentType;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Scanner;

public class HTTPServer {

    public static final int PORT = 80;
    public static final String IP = "127.0.0.1";
    public static final String CRLF = "\r\n";
    public static final String EOH = CRLF + CRLF;

    public static void main(String[] args){

        System.out.println("server is listening to port 80");
        try {
            ServerSocket serverSocket = new ServerSocket(PORT);

            while(true){
                //socket connection
                Socket socket = serverSocket.accept();
                System.out.println("get connection from IP: " + socket.getRemoteSocketAddress());

                DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
                DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());



                //print incoming HTTP request and save it as one string
                //parse through the HTTP request to get the host/server address
                //store the following string which may or may not be a file name or path
                // good to handle strings from stream
                String hostAd = "";
                String wanted = "";

                BufferedReader bufferedReader = new BufferedReader( new InputStreamReader(dataInputStream));
                String line = bufferedReader.readLine();

                //read the host line to get the host address and the file name or path if its provided

                System.out.println(line); //debugging
                Scanner scanGet = new Scanner(line);
                scanGet.useDelimiter(" ");

                String GET = scanGet.next();
                wanted = scanGet.next();

                scanGet.close();

                while(line != null && !line.isEmpty()){
                    System.out.println(line);
                    line  = bufferedReader.readLine();

                    //read the host line to get the host address and the file name or path if its provided
                    if (line.contains("Host: ")){
                        Scanner scanLine = new Scanner(line);
                        scanLine.useDelimiter(" ");

                        String noUse = scanLine.next();
                        hostAd = scanLine.next();

                        scanLine.close();
                    }


                }
                System.out.println(hostAd + " " + wanted);


                PrintWriter printWriter = new PrintWriter(new OutputStreamWriter(dataOutputStream, StandardCharsets.ISO_8859_1));
                String wantedFileName = "";

                System.out.println("Wanted: "+ wanted);
                if (wanted.equals("")) {
                    wanted = "/";
                }
                //if both the hostAddress and a file name/path is given
                if (hostAd.equals(IP) && !(wanted.equals("/"))){
                    System.out.println("This if is running.");

//                    if (wanted.contains("\\")){
//                        wantedFileName = "server_folder\\" + wanted;
//                    }

                    if (wanted.contains("\\")){
                        wanted = "server_folder\\" + wanted;
                    }
                    else if (wanted.contains("/")){
                        String newWanted = "server_folder";


                        Scanner scanSlash = new Scanner(wanted);
                        scanSlash.useDelimiter("/");

                        while (scanSlash.hasNext()){
                            newWanted += "\\" + scanSlash.next();
                        }


                        wanted = newWanted;
                        scanSlash.close();

                    }


                    File folderMain = new File("server_folder");
                    File[] contentsMain = folderMain.listFiles();

                    Boolean fileExists = false;

                    for(int i = 0; i < contentsMain.length; i++){

                        if (fileExists){
                            break;
                        }
                        File contentFile = contentsMain[i];

                        if(contentFile.getPath().contains(wanted)){
                            fileExists = true;
                            wantedFileName =  contentFile.getPath();
                        }
                        else{
                            File[] cm = contentFile.listFiles();
                            if (cm != null){
                                for(int j = 0; j < cm.length; j++) {
                                    File cmFile = cm[j];

                                    if (cmFile.getPath().contains(wanted)) {
                                        fileExists = true;
                                        wantedFileName = cmFile.getPath();
                                        System.out.println(wantedFileName);
                                        break;
                                    }
                                }
                            }
                        }
                    }

                    //if the file was not found, generate and send the HTTP 404 error
                    if (fileExists == false){
                        //generate the HTTP reponse and send out the data
                        printWriter.print("HTTP/1.1 404 Not Found" + CRLF);
                        printWriter.print("Content-type: " + "text/html" + CRLF);
                        printWriter.print("Content-Length: " + 23 + CRLF);
                        printWriter.print(CRLF);
                        printWriter.flush();



                        bufferedReader.close();
                        printWriter.close();
                        dataInputStream.close();
                        dataOutputStream.close();
                        break;
                    }

                }
                //if there is only the hostAddress and no file name/path given
                else if (hostAd.equals(IP) && wanted.equals("/")){
                    System.out.println("This else if is running.");

                    //put together the full file name and get the file
                    wantedFileName = "server_folder\\" + "index.html";
                }

                System.out.println(wantedFileName);



                File sendFile = new File(wantedFileName);
                int sendFileLen = (int)sendFile.length();
                System.out.println(sendFileLen);

                //get all data from the file
                FileInputStream inputStream = new FileInputStream(sendFile);
                byte[] bytesToSend = new byte[sendFileLen];
                int NofBytesRead1 = 0;
                while(NofBytesRead1 != sendFileLen){
                    int readBytes = inputStream.read(bytesToSend, NofBytesRead1, sendFileLen - NofBytesRead1);
                    NofBytesRead1 += readBytes;
                }

                //get the type of the file to put in the HTTP request
                String type = Files.probeContentType(Path.of(wantedFileName));

                //generate the HTTP reponse and send out the data
                printWriter.print("HTTP/1.1 200 OK" + CRLF);
                printWriter.print("Date: Mon, 6-May-24 11:15:24 GMT" + CRLF);
                printWriter.print("Server: Project2Server" + CRLF);
                printWriter.print("Content-type: " + type + CRLF);
                printWriter.print("Content-Length: " + sendFileLen + CRLF);
                printWriter.print(CRLF);
                printWriter.flush();
                //dataOutputStream.write(bytesToSend, 0, sendFileLen);
                int NofBytesReadOut = 0;
                while(NofBytesReadOut != sendFileLen){
                    //I have no idea why this error keeps happening
                    //I think it's just a one-off error
                    //so this is my temporary fix
                    if (NofBytesReadOut == -1){
                        break;
                    }
                    dataOutputStream.write(bytesToSend, NofBytesReadOut, sendFileLen - NofBytesReadOut);
                    NofBytesReadOut += sendFileLen - NofBytesReadOut;
                }

                inputStream.close();


				
				bufferedReader.close();
                printWriter.close();
                dataInputStream.close();
                dataOutputStream.close();

                //close socket?

                break;
            }

        }catch (UnknownHostException e){
            e.printStackTrace();
        }catch (IOException e){
            e.printStackTrace();
        }


    }




//    private static String fileE(File[] contents, String wanted){
//        String fullPath = "";
//        Boolean fileExists = false;
//
//        for(int i = 0; i < contents.length; i++){
//            File contentFile = contents[i];
//            System.out.println(contents[i].getPath());
//
//            if(contentFile.getPath().contains(wanted)){
//                fileExists = true;
//                fullPath =  contentFile.getPath();
//            }
//            else{
//                if (contentFile.listFiles() != null){
//                    fileE(contentFile.listFiles(), wanted);
//                }
//            }
//        }
//
//        return fullPath;
//    }
}



