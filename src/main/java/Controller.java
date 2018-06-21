import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.jfoenix.controls.*;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import org.mortbay.util.ajax.JSONObjectConvertor;

public class Controller {
    // GLOBAIS //
    Thread t1;
    Search Pesquisa;
    List<String> ID = new ArrayList<>();
    String dir;
    List<JFXSpinner> titles2 = new ArrayList<>();
    // GLOBAIS //
    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private JFXTextField Pesquisar;

    @FXML
    private GridPane Grid = new GridPane();

    @FXML
    private AnchorPane Principal;

    @FXML
    private AnchorPane scroll;

    @FXML
    private JFXTextField path;

    @FXML
    private JFXComboBox<String> Formato;

    @FXML
    void Ok(ActionEvent event) throws InterruptedException {
        //search(Pesquisar.getText());

        try{

        if (Pesquisar.getText().substring(0,38).equals("https://www.youtube.com/playlist?list=")){

            List<Label> titles = new ArrayList<>();



            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    String link = Pesquisar.getText();//"http://youtube.com/watch?v=" + ID.get(i);
                    //Formato.getSelectionModel());
                    /* Calls Python */
                    String cmd = "python C:\\Users\\Thiago\\IdeaProjects\\DitiDownloader\\src\\main\\java\\YoutubeLink.py " + link ;
                    System.out.println(cmd);
                    String line = "";
                    try {
                        Process p = Runtime.getRuntime().exec(cmd);
                        BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream(), "Windows-1252"));
                        String aux;
                        int i = 0 ;
                        while (true) {
                            aux = line;
                            line = r.readLine();
                            if (line == null) {
                                line = aux;
                                break;
                            }
                           //
                            try {
                                String titulo = GetTitle(line.substring(31));
                                titles.add(new Label(titulo));
                                Grid.add(titles.get(i), 2, i);
                                i++;
                            }catch (Exception e){

                            }

                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    Platform.runLater(() -> scroll.getChildren().add(Grid));

                }
            });
            thread.start();



        }
        } catch (Exception e ){}




    }

    @FXML
    void search(String Texto) {
        Pesquisa = new Search(Texto);
        t1 = new Thread(Pesquisa);
        t1.start();

        Thread t = new Thread(new Runnable() {
            public void run() {
                try {
                    t1.join();
                    //Platform.runLater(() -> scroll.getChildren().remove(Grid));

                    for (String title : Pesquisa.getID()) {
                        ID.add(title);
                    }


                    List<Label> titles = new ArrayList<>();
                    for (String title : Pesquisa.getLista()) {
                        titles.add(new Label(title));
                    }

                    List<ImageView> thumbs = new ArrayList<>();

                    for (String url : Pesquisa.getThumb()) {
                        thumbs.add(new ImageView(new Image(url)));
                    }

                    for (int i = 0; i != thumbs.size(); i++) {

                        thumbs.get(i).setFitHeight(100);
                        thumbs.get(i).setFitWidth(100);

                        thumbs.get(i).setOnMouseClicked(action(i));
                        titles.get(i).setOnMouseClicked(action(i));
                        titles2.add(new JFXSpinner());

                        Grid.add(thumbs.get(i), 0, i);
                        Grid.add(titles.get(i), 2, i);
                        Grid.add(titles2.get(i), 1, i);

                    }
                    Platform.runLater(() -> scroll.getChildren().add(Grid));


                } catch (Exception e) {
                    e.printStackTrace();
                    System.out.println(e);
                }
            }
        });
        t.start();
    }

    // Set action on ImageView after search Video of Youtube
    private EventHandler<? super MouseEvent> action(int i) {

        EventHandler<MouseEvent> action = new EventHandler<MouseEvent>() {

            @Override
            public void handle(MouseEvent event) {

                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        String link = "http://youtube.com/watch?v=" + ID.get(i);
                        //Formato.getSelectionModel());
                        /* Calls Python */
                        String cmd = "python C:\\Users\\Thiago\\IdeaProjects\\DitiDownloader\\src\\main\\java\\DitiPytube.py " + link + " \"" + dir + "\" True," + Formato.getSelectionModel().getSelectedItem();
                        System.out.println(cmd);
                        String line = "";
                        try {
                            Process p = Runtime.getRuntime().exec(cmd);
                            BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream(), "Windows-1252"));
                            String aux;
                            while (true) {
                                aux = line;
                                line = r.readLine();
                                if (line == null) {
                                    line = aux;
                                    break;
                                }
                                System.out.println(line);

                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        String path = dir + "\\" + line;
                        System.out.println(path);
                        Converte(path, i);

                    }
                });
                thread.start();
            }
        };

        return action;
    }

    @FXML
    void salvarem(ActionEvent event) {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Selecione onde quer salvar");
        directoryChooser.setInitialDirectory(new File(System.getProperty("user.home")));
        dir = directoryChooser.showDialog(null).toString();
        path.setText(dir);
    }

    void Converte(String dir, int i) {
        Float Total = GetDuration(dir);
        System.out.println(Total);
        try {
            String cmd = "ffmpeg  -i " + dir + " -progress - -y " + dir + ".mp3";
            ProcessBuilder builder = new ProcessBuilder("ffmpeg", "-i", dir, "-progress", "-", "-y", dir + ".mp3");
            builder.redirectErrorStream(true);
            Process p = builder.start();
            BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line = "";
            while (true) {
                line = r.readLine();
                if (line == null) {
                    break;
                }
                try {
                    Float calc = GetDuration(dir + ".mp3");
                    Float porctagem = (calc) / Total;
                    if (i != -1) {
                        Platform.runLater(() -> titles2.get(i).setProgress(porctagem));
                    }
                    System.out.println(porctagem);

                } catch (Exception e) {

                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    @FXML
    void getmusic(ActionEvent event) {
        FileChooser directoryChooser = new FileChooser();
        directoryChooser.setTitle("Selecione onde quer salvar");
        directoryChooser.setInitialDirectory(new File(System.getProperty("user.home")));
        dir = directoryChooser.showOpenDialog(null).toString();
        path.setText(dir);

        Converte(dir, -1);

    }

    private Float GetDuration(String path) {
        /* Calls FFPROBE */
        String FFPROBE = "ffprobe -i \"" + path + "\" -v quiet -print_format json -show_format -show_streams -hide_banner";
        System.out.println(FFPROBE);
        String aux = "";
        try {
            Process p = Runtime.getRuntime().exec(FFPROBE);
            BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line2 = "";
            while (true) {
                aux += line2;
                line2 = r.readLine();
                if (line2 == null) {
                    break;
                }
                //System.out.println(line2);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        JsonObject contentObj = new JsonParser().parse(aux).getAsJsonObject();
        JsonArray string = contentObj.getAsJsonArray("streams");
        JsonElement element = string.get(0);
        String durationString = element.getAsJsonObject().get("duration").toString();
        float duration = Float.parseFloat(durationString.replaceAll("\"", ""));
        //JsonArray string2 = string.getAsJsonArray("duration");

        //System.out.println(duration);
        return duration;

    }


    @FXML
    void initialize() {
        Formato.getItems().add("MP4");
        Formato.getItems().add("MP3");



        //String YOUR_VIDEO_ID = "rn_YodiJO6k";


        //String titulo = GetTitle(YOUR_VIDEO_ID);

        //System.out.println(titulo);
        System.out.println();

    }
    //url = https://www.googleapis.com/youtube/v3/playlists?part=id&channelId=UC8ughefz4kcazXZBKBEhZww&key=AIzaSyDGRbEc7qbGJ59Vsv68fL0aHml1FYpX_1g get playlist of user

    String GetTitle(String VideoID) {

        String ur2l = "https://www.googleapis.com/youtube/v3/videos?part=id%2C+snippet&id=" + VideoID + "&key=AIzaSyDGRbEc7qbGJ59Vsv68fL0aHml1FYpX_1g";
        String titulo = "";
        try {
            URL url = new URL(ur2l);
            URLConnection request = url.openConnection();
            request.connect();

            // Convert to a JSON object to print data
            JsonParser jp = new JsonParser(); //from gson
            JsonElement root = jp.parse(new InputStreamReader((InputStream) request.getContent())); //Convert the input stream to a json element
            JsonObject rootobj = root.getAsJsonObject();
            JsonArray string = rootobj.getAsJsonArray("items");//May be an array, may be an object.
            //System.out.println(string);
            JsonElement element = string.get(0);
            JsonElement element2 = element.getAsJsonObject().get("snippet");
            titulo = element2.getAsJsonObject().get("title").toString();
            System.out.println(titulo);
            //Principal.getChildren().add(listView);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return titulo;
    }
}




