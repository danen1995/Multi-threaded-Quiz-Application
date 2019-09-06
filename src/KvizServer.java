
import java.io.*;
import java.net.*;
import java.util.LinkedList;
import java.util.Random;
import kviz.GrupaPitanja;
import kviz.Pitanje;

public class KvizServer {
    // lista server niti, za svakog novog korisnika pravimo po jednu 

    public static int i = 0;
    static LinkedList<ServerNit> klijenti = new LinkedList<ServerNit>();
    public static GrupaPitanja grupa = new GrupaPitanja();

    public static void main(String[] args) throws IOException {
        ucitajTXTFajl();

        int port = 2222;
        if (args.length > 0) {
            port = Integer.parseInt(args[0]);
        }

        // soket klijenta koji ce doci na server
        Socket klijentSoket = null;
        try {
            // ovde otvaramo serverski soket na portu 2222 
            ServerSocket serverSoket = new ServerSocket(port);
            while (true) {
                // accept ceka klijenta, kada klijent dodje, kao rezultat vraca soket koji predstavlja 
                // vezu sa tim klijentom
                klijentSoket = serverSoket.accept();
                // pravimo novu nit koja ce raditi sa klijentom i ubacujemo je na kraj liste
                klijenti.addLast(new ServerNit(klijentSoket, klijenti));
                klijenti.getLast().start();
            }
        } catch (IOException e) {
            System.out.println(e);
        }
    }

    public static void zapocniIgru(ServerNit prvi, ServerNit drugi) throws IOException {

        int brPitanja = 1;
        while (brPitanja <= 10) {
            posaljiRandomPitanje(prvi, drugi, brPitanja);
            razmeniOdgovoreKlijenata(prvi, drugi);
            brPitanja++;
        }
        prvi.prikaziRezultat();
        drugi.prikaziRezultat();
    }

    private static void razmeniOdgovoreKlijenata(ServerNit prvi, ServerNit drugi) throws IOException {
        prvi.posaljiOdgovorProtivniku(drugi);
        drugi.posaljiOdgovorProtivniku(prvi);
    }

    private static void posaljiRandomPitanje(ServerNit prvi, ServerNit drugi, int rbrPitanja) throws IOException {
        Pitanje p = vratiRandomPitanje();
        prvi.posaljiPitanje(p, rbrPitanja);
        drugi.posaljiPitanje(p, rbrPitanja);

    }

    private static Pitanje vratiRandomPitanje() {
        return vratiPitanja().get(nasumicniRedniBrojPitanja());
    }

    private static void ucitajTXTFajl() throws IOException {
        BufferedReader reader = null;
        reader = new BufferedReader(new FileReader("pitanja.txt"));
        String line;
        String question = "", choiceA = "", choiceB = "", choiceC = "", choiceD = "", answer = "";
        while ((line = reader.readLine()) != null) {

            if (line.contains("?")) {
                question = line;
                continue;
            }

            if (line.contains("a)")) {
                choiceA = line;
                continue;
            }

            if (line.contains("b)")) {
                choiceB = line;
                continue;
            }

            if (line.contains("c)")) {
                choiceC = line;
                continue;
            }

            if (line.contains("d)")) {
                choiceD = line;
                continue;
            }

            answer = line;
            String[] ponudjeniOdgovori = {choiceA, choiceB, choiceC, choiceD};
            Pitanje pitanje = new Pitanje(question, ponudjeniOdgovori, answer);
            grupa.unesiPitanje(pitanje);
        }

    }

    public static LinkedList<Pitanje> vratiPitanja() {
        return grupa.getPitanja();
    }

    public static int nasumicniRedniBrojPitanja() {
        Random rand = new Random();
        int randomNum = rand.nextInt((grupa.getPitanja().size()));
        return randomNum;
    }

    public static void upisiUDatoteku(String s, String nazivDatoteke) {
        try {
            PrintStream out = new PrintStream(new FileOutputStream(nazivDatoteke, true));
            out.println(s);
            out.close();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            System.out.println(e.getMessage());
        }

    }
}
