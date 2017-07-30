package audiomanager;

import java.awt.Desktop;
import java.util.Scanner;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import audiomanager.AudioManagerException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.mp3.LyricsHandler;
import org.apache.tika.parser.mp3.Mp3Parser;
import org.apache.tika.sax.BodyContentHandler;
import java.io.StringWriter;
import org.xml.sax.SAXException;
import java.io.BufferedWriter;
import org.apache.tika.parser.audio.AudioParser;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import java.io.FileWriter;
import static java.lang.System.exit;

public class AudioManager {

    private String currentDirectory;
    private String[] extensii = {".mp3", ".wav", ".flac"};
    ArrayList<String> fav = new ArrayList();

    public AudioManager() {
        currentDirectory = System.getProperty("user.dir");
        System.out.println("Working Directory = " + currentDirectory);
    }

    public void setCurrentDirectory(String directoryPath) {
        try {
            if (new File(directoryPath).exists()) {
                currentDirectory = directoryPath;
            } else {
                throw new AudioManagerException("Calea de acces ese invalida!");
            }
        } catch (AudioManagerException e) {
            System.out.println(e.getMessage());
        }
    }

    public String getCurrentDirectory() {
        return currentDirectory;
    }

    public void cd(String unDirector) {
        char[] caractereInterzise = {'>', '<', '?', '*', ':', '|', '/', '\\', '"'};

        try {
            if (unDirector.equals("..")) {
                if (currentDirectory.contains("\\")) {
                    this.setCurrentDirectory(currentDirectory.substring(0, currentDirectory.lastIndexOf("\\")));
                } else {
                    throw new AudioManagerException("Esti in directorul radacina!");
                }
                return;
            }
            if (Pattern.matches("[.]+", unDirector)) {
                return;
            }
            /*for (int i = 0; i < caractereInterzise.length; i++) {
                if (unDirector.indexOf(caractereInterzise[i]) == 0) {
                    throw new AudioManagerException("Numele directorului nu este intr-un format corespunzator!");
                }
            }*/

            File directorPath = new File(currentDirectory + "\\" + unDirector);

            if (directorPath.exists() && directorPath.isDirectory()) {
                currentDirectory = directorPath.toString();
            } else {
                throw new AudioManagerException("Director inexistent!");
            }
        } catch (AudioManagerException e) {
            System.out.println(e.getMessage());

        }

    }
    
    private boolean verifyExtension(File file) {
        for (String extensie : extensii) {
            if (file.getName().toLowerCase().endsWith(extensie))
                return true;  
        }
        return false;
    }

    public void list(String directory) {
        // System.out.println("Am intrat sa listam!" + directory + "*");
        if (directory.isEmpty()) {
            directory = currentDirectory;
        }
        try {
            if (!directory.contains("\\")){
                directory = currentDirectory + "\\" + directory;
            }

            File dir = new File(directory);
            if (!(dir.exists() && dir.isDirectory())) {
                throw new AudioManagerException("Director inexistent!");
            }
            File[] files = dir.listFiles();
            
            for (File file : files) {
                if (file.isFile()) {
                    if(verifyExtension(file)){
                        System.out.println(file.getName());      
                    }
                }
            }

        } catch (AudioManagerException e) {
            System.out.println(e.getMessage());
        }

    }

    public void find(String deCautat, String directory) {
        if (directory.isEmpty()){
            directory = currentDirectory;
        }
        if (!isDirectory(directory)){
            return ;
        }
        deCautat = deCautat.toLowerCase();
        File dir = new File(directory);
        File[] list = dir.listFiles();

        if (list == null) {
            return;
        }
        Pattern filePattern = Pattern.compile(deCautat);

        for (File f : list) {
            if (f.isDirectory()) {
                find(deCautat, f.getAbsolutePath());
                //System.out.println(f.getAbsolutePath());
            } else if (verifyExtension(f)) {
                Matcher patternPart = filePattern.matcher(f.getName().toLowerCase());
                if (patternPart.find()) {
                    System.out.println(f.getAbsolutePath().replace(currentDirectory, "") + f.getName());
                }
            }
        }
    }

    private boolean isDirectory(String directory) {
        try {
            if (!directory.contains("\\")) {
                directory = currentDirectory + "\\" + directory;
            }
            File dir = new File(directory);
            if (!(dir.exists() && dir.isDirectory())) {
                throw new AudioManagerException("Director inexsitent!");
            }

        } catch (AudioManagerException e) {
            System.out.println(e.getMessage());
            return false;
        }
        return true;

    }
    

    /*    public void find(String pattern){
        findRelativDir(currentDirectory,pattern);
    }
    
     */
    public void play(String file) {
        try {
            if (!verifyExtension(new File(file))) {
                throw new AudioManagerException("Extensie necorespunzatoare!");
            } else {
                String path = currentDirectory + "\\" + file;
                System.out.println(path);
                File fisier = new File(path);
                try {
                    Desktop.getDesktop().open(fisier);
                } catch (IOException e) {
                    System.out.println(e.getMessage());
                }
            }
        } catch (AudioManagerException e) {
            System.out.println(e.getMessage());
        }
    }

    public String get() {

        return currentDirectory;

    }

    public void info(String file){ //throws Exception, IOException, SAXException, TikaException

        try {
            BodyContentHandler handler = new BodyContentHandler();
            Metadata metadata = new Metadata();
            File newFile = new File(file);
            FileInputStream inputstream = new FileInputStream(newFile);
            ParseContext pcontext = new ParseContext();

            if (verifyExtension(newFile)) {
                Mp3Parser Mp3Parser = new Mp3Parser();
                Mp3Parser.parse(inputstream, handler, metadata, pcontext);
            } else if (verifyExtension(newFile)) {
                AudioParser Mp3Parser = new AudioParser();
                Mp3Parser.parse(inputstream, handler, metadata, pcontext);
            } else {
                throw new AudioManagerException("Extensie necorespunzatoare");
            }

            LyricsHandler lyrics = new LyricsHandler(inputstream, handler);

            while (lyrics.hasLyrics()) {
                System.out.println(lyrics.toString());
            }

            System.out.println("Contents of the document:" + handler.toString());
            System.out.println("Metadata of the document:");
            String[] metadataNames = metadata.names();

            for (String name : metadataNames) {
                System.out.println(name + ": " + metadata.get(name));
            }
        } catch (AudioManagerException e) {
            System.out.println(e.getMessage());
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public void addFav(String fisier) {
        try {
            if (fisier.matches("([^\\s]+(\\.(?i)(wav|flac|mp3))$)")) {
                fav.add(fisier);
                //this.serializare();
            } else {
                throw new AudioManagerException("Extensie necorespunzatoare!");
            }
        } catch (AudioManagerException e) {
            System.out.println(e.getMessage());
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public void serializare() {

        try {
            FileOutputStream fos = new FileOutputStream("favorite.txt");
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(fav);
            oos.close();
            fos.close();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    public void deserializare() {
        ArrayList<String> listFav = new ArrayList<String>();
        try {
            FileInputStream fis = new FileInputStream("favorite.txt");
            ObjectInputStream ois = new ObjectInputStream(fis);
            listFav = (ArrayList) ois.readObject();
            ois.close();
            fis.close();
            // return listFav;
        } catch (IOException ioe) {
            ioe.printStackTrace();
        } catch (ClassNotFoundException c) {
            System.out.println("Class not found");
            c.printStackTrace();
        }
        for (String i : listFav) {
            System.out.println(i);
        }
    }

    void showFavorite() {
        System.out.println(fav.toString());
    }

    public ArrayList getFav() {

        return fav;
    }

    public void generateHTML(String str) {

        VelocityEngine ve = new VelocityEngine();
        ve.init();
        Template t = ve.getTemplate("Report.vm");

        VelocityContext context = new VelocityContext();
        context.put("fav", this.getFav());

        StringWriter writer = new StringWriter();
        t.merge(context, writer);

        try {
            BufferedWriter out = new BufferedWriter(new FileWriter(str));
            out.write(writer.toString());
            out.close();
        } catch (IOException e) {
            System.out.println("Exception ");
        }
    }

    /*File file = new File(str);
        BufferedWriter htmlWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file)));
        htmlWriter.write(text);
        htmlWriter.flush();
        htmlWriter.close();
    }/*/
    public void run() {
        String input, comanda, parametru;

        Scanner in = new Scanner(System.in);

        while (true) {
            System.out.print("\n" + this.getCurrentDirectory() + "> ");
            input = in.nextLine();

            if (input.indexOf(" ") > 0) {
                comanda = input.substring(0, input.indexOf(" "));
                parametru = input.substring(input.indexOf(" ") + 1);
            } else {
                comanda = input;
                parametru = "";
            }

            switch (comanda) {
                case "cd":
                    cd(parametru);
                    break;
                case "ls":
                    list(parametru);
                    break;
                case "find":
                    find(parametru,"");
                    break;
                case "play":
                    play(parametru);
                    break;
                case "info":
                    info(parametru);
                    break;
                case "exit":
                    exit(0);
                    break;
                default:
                    System.out.println("Comanda inexistenta\nComenzile posibile: cd, ls, find, play, info");
            }
        }

    }

    public void comanda() {
        Scanner scanIn = new Scanner(System.in);

        String command;
        System.out.println("Comenzile posibile sunt: cd, list, find, play, fav ");

        while (true) {
            command = scanIn.next();
            if (command.equalsIgnoreCase("cd")) {
                System.out.println("Introduceti subdrectorul");
                command = scanIn.next();
                this.cd(command);
                System.out.println("Noul director curent:" + this.get());
            } else if (command.equalsIgnoreCase("list")) {
                //this.list();
            } else if (command.equalsIgnoreCase("find")) {
                System.out.println("Introduceti patternul de cautat: ");
                command = scanIn.next();
                //this.find(command);
            } else if (command.equalsIgnoreCase("play")) {
                System.out.println("Fisierul pe care doriti sa-l deschideti: ");
                command = scanIn.next();
                this.play(command);
            } else if (command.equalsIgnoreCase("fav")) {
                System.out.println("Fisierul pe care doriti sa-l adaugati la favorite: ");
                command = scanIn.next();
                this.addFav(command);
                System.out.println("Acestea sunt favoritele tale: ");
                this.showFavorite();
            } else {
                System.out.println("Comanda gresita (cd,list,find,play,report,fav)");
            }
        }
    }

    public static void main(String args[]) throws Exception, IOException, SAXException, TikaException {
        AudioManager d = new AudioManager();
        d.run();
        /*   //Afisam directorul curent
    	 System.out.println("Directorul curent specificat prin calea absoluta] "+d.get());
    	 System.out.println("-----------------------");
    	//Schimbam directorul curent cu un altul relativ la cel curent (un subdirector al directorului actual)
    	 	 
    	 d.cdRel("director");
    	 System.out.println("Noul director [specificat prin calea absoluta]  "+d.get());
    	 System.out.println("-----------------------");
    	//Listam fisierele audio din directorul curent
    	 System.out.println("Listam fisierele audio din directorul curent ");
    	 d.listCurrentDirectory();	 
    	 System.out.println("-----------------------");
         //Listam fisierele dintr-un anumit director 
    	 System.out.println("Listam fisierele din directorul ales  ");
         d.listSpecifiedDirectory("C:\\Users\\lucia_000\\workspace\\pa_Lab4");
         System.out.println("-----------------------");
        //schimbare directorului curent 
         d.cd("C:\\Users\\lucia_000\\workspace\\pa_Lab4");
         System.out.println("Directorul curent: "+d.get());
         System.out.println("-----------------------");
         //Cautam o melodie dupa numele trupei, in mod recursiv din directorul curent, trecand prin subdirectoare
    	 d.find("Demon");
    	 System.out.println("Astfel " +d.get());
    	 System.out.println("-----------------------");
    	//Pornim o melodie din directorul curent
    	d.play("Cosmic_Dancer-T.wav");
    	//Verifica exceptia: fisierul nu are extensia corespunzatoare (.mp3,.wav,.flac)
        d.play("floare.jpeg");
    	System.out.println("-----------------------");
    	//Afisam metadatele despre un anumit fisier
    	d.info("Rammstein-Engel.mp3");
    	System.out.println("-----------------------");
    	System.out.println("Se adauga un fisier in lista cu melodii favorite");
    	d.addFav("Ramstein-Engel.mp3");
    	d.addFav("Nirvana_Lake_of_Fire.flac");
    	System.out.println("-----------------------");
    	System.out.println("Afisam lista de favorite  ");
    	d.showFavorite();  
    	System.out.println("-----------------------");
    	System.out.println("Serializare [ a se vedea fisierul favorite.txt] ");
    	d.serializare();
    	System.out.println("-----------------------");
    	System.out.println("Deserializare cu afisare ");
    	d.deserializare();
    	
       //System.out.println(writer);
        
    	
        System.out.println("----------------");
        System.out.println("Am generat raportul in format html");
        d.generateHTML( "report.html");
        
        d.cd("abc");
         */
        //d.comanda();
        //Cosmic_Dancer-T.wav
        //floare.jpeg
        //Nirvana_Lake_of_Fire.flac
        //Ramstein-Engel.mp3
        //d.info("Rammstein-Engel.mp3");
    }

}
