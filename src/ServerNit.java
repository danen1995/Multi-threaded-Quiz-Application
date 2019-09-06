
import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.util.LinkedList;

import kviz.Pitanje;

public class ServerNit extends Thread {

    BufferedReader ulazniTokOdKlijenta = null;
    PrintStream izlazniTokKaKlijentu = null;
    Socket soketZaKom = null;
    LinkedList<ServerNit> klijenti;
    String ime;
    String[] komeSaljem;
    boolean nemaOnline = false;
    String odgovorKorisnika;

    public ServerNit(Socket soket, LinkedList<ServerNit> klijent) {
        this.soketZaKom = soket;
        this.klijenti = klijent;
    }

    public void run() {
        try {
            ulazniTokOdKlijenta = new BufferedReader(new InputStreamReader(soketZaKom.getInputStream()));
            izlazniTokKaKlijentu = new PrintStream(soketZaKom.getOutputStream());
            unosImena();
            while (true) {
                prikazOnlineOsoba();
                if (nemaOnline) {
                    // ako nema online korisnika, moguce su tri opcije, da korisnik odluci da ugasi prozor
                    // da mu stigne zahtev od drugog korisnika (koji moze da prihvati ili odbije) 
                    // i da klikne na dugme osvezi kako bi proverio da li se neko u medjuvremnu ulogovao
                    String s = ulazniTokOdKlijenta.readLine();

                    if (s.equals("Ugasio prozor")) {
                        klijenti.remove(this);
                        return;
                    }

                    if (s.startsWith("YES")) {
                        izlazniTokKaKlijentu.println("Pocinje igra");
                        String posiljalac = s.split(" ")[1];
                        vratiServerNit(posiljalac).izlazniTokKaKlijentu.println("signal-prihvatiozahtev");
                        //Ovu poruku prima ServerNit koja salje zahtev(posiljaoc), u metodi prihvatioZahtev.
                        //Poruka se najpre salje KvizKlijent-u - posiljaocu, a zatim on  
                        //prosledjuje tu poruku svojoj ServerNiti (Server nit posiljaoc)
                        break;
                    }

                    if (s.equals("NO")) {
                        izlazniTokKaKlijentu.println("Zahtev za igru je odbijen.");
                        String posiljalac = s.split(" ")[1];
                        vratiServerNit(posiljalac).izlazniTokKaKlijentu.println("Vas zahtev je odbijen.");
                        continue;
                    }

                    if (kliknuoOsvezi(s)) {
                        continue;
                    }
                }
                izlazniTokKaKlijentu.println("Napisite nickname osobe sa kojom zelite da igrate");
                String odabrani;
                String s = ulazniTokOdKlijenta.readLine();

                if (s.equals("Ugasio prozor")) {
                    klijenti.remove(this);
                    return;
                }

                if (!kliknuoOsvezi(s)) {
                    odabrani = s;
                } else {
                    continue;
                }

                if (s.startsWith("YES")) {//ovde treba da izbacimo tog igraca iz liste klijenata??
                    izlazniTokKaKlijentu.println("Pocinje igra");
                    String posiljalac = s.split(" ")[1];
                    vratiServerNit(posiljalac).izlazniTokKaKlijentu.println("signal-prihvatiozahtev");
                    //Ovu poruku prima ServerNit koja salje zahtev(posiljaoc), u metodi prihvatioZahtev.
                    //Poruka se najpre salje KvizKlijent-u - posiljaocu, a zatim on  
                    //prosledjuje tu poruku svojoj ServerNiti (Server nit posiljaoc)
                    break;
                }

                if (s.equals("NO")) {
                    izlazniTokKaKlijentu.println("Zahtev za igru je odbijen.");
                    String posiljalac = s.split(" ")[1];
                    vratiServerNit(posiljalac).izlazniTokKaKlijentu.println("Vas zahtev je odbijen.");
                    continue;
                }

                boolean postoji = false;
                for (int j = 0; j < onlineKorisnici().size(); j++) {
                    if (odabrani.equals(onlineKorisnici().get(j).ime)) {
                        postoji = true;
                        if (daLiJeOnline(onlineKorisnici().get(j))) {
                            ServerNit prvi = this;
                            ServerNit drugi = onlineKorisnici().get(j);
                            drugi.izlazniTokKaKlijentu.println("Stigao je zahtev za igru sa " + this.ime + " Da li zelite da prihvatite zahtev?");
                            izlazniTokKaKlijentu.println("Molimo sacekajte odgovor protivnika: " + odabrani);

                            if (prihvatioZahtev(drugi)) {
                                klijenti.remove(prvi);
                                KvizServer.zapocniIgru(prvi, drugi);
                                return;

                            } else {
                                break;
                            }
                        } else {
                            izlazniTokKaKlijentu.println("Korisnik " + onlineKorisnici().get(j).ime + " se u medjuvremenu odjavio. Izabrite ponovo korisnika.");
                        }

                    }
                }
                if (postoji == false) {
                    izlazniTokKaKlijentu.println("Osoba " + odabrani + " ne postoji. Pogresili ste!");

                }

            }
//			izlazniTokKaKlijentu.println("Dovidjenja. Dodjite nam ponovo - " + ime);
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        for (int i = 0; i < klijenti.size(); i++) {
            if (klijenti.get(i) == this) {
                klijenti.remove(i);
            }
        }
    }

    private boolean kliknuoOsvezi(String s) throws IOException {
        if (s.equals("PrikaziOnline")) {
            return true;
        }
        return false;
    }

    private boolean prihvatioZahtev(ServerNit drugi) throws IOException {
        if (ulazniTokOdKlijenta.readLine().startsWith("signal-prihvatiozahtev")) {
            return true;
        }
        return false;
    }

    private void prikazOnlineOsoba() {
        if (onlineKorisnici().isEmpty()) {
            izlazniTokKaKlijentu.println("Zao nam je. Trenutno nema online korisnika. Pokusajte kasnije.");
            nemaOnline = true;
            return;
        }
        nemaOnline = false;
        izlazniTokKaKlijentu.println("Online osobe su:");

        for (ServerNit serverNit : onlineKorisnici()) {
            izlazniTokKaKlijentu.println(serverNit.ime);
        }

    }

    private LinkedList<ServerNit> onlineKorisnici() {
        LinkedList<ServerNit> onlineKorisnici = new LinkedList<ServerNit>();

        for (int i = 0; i < klijenti.size(); i++) {
            if (!klijenti.get(i).equals(this)) {
                onlineKorisnici.add(klijenti.get(i));
            }
        }
        return onlineKorisnici;
    }

    private void unosImena() throws IOException {
        // saljemo klijentu poruku da unese nickname
        izlazniTokKaKlijentu.println("Unesite nickname.");
        // cekamo da klijent unese ime
        String probnoIme = ulazniTokOdKlijenta.readLine();
        while (daLiPostojiTajNadimak(probnoIme)) {
            izlazniTokKaKlijentu.println("Osoba sa tim imenom vec postoji. Unesite drugo ime.");
            probnoIme = ulazniTokOdKlijenta.readLine();
        }
        ime = probnoIme;
        izlazniTokKaKlijentu.println(ime + ", dobrodosli u kviz Upoznaj Evropu.");
        upisiUDatoteku(ime + "\n", "klijenti.txt");

    }

    private boolean daLiPostojiTajNadimak(String ime) {
        for (int i = 0; i < klijenti.size() - 1; i++) {
            if (klijenti.get(i).ime.equals(ime)) {
                return true;
            }
        }
        return false;
    }

    private boolean daLiJeOnline(ServerNit serverNit) {
        for (int i = 0; i < klijenti.size(); i++) {
            if (klijenti.get(i).equals(serverNit)) {
                return true;
            }
        }
        return false;
    }

    public ServerNit vratiServerNit(String s) {
        for (ServerNit serverNit : onlineKorisnici()) {
            if (s.equals(serverNit.ime)) {
                return serverNit;
            }
        }
        return null;

    }

    private void prikaziListu(LinkedList<ServerNit> lista) {
        izlazniTokKaKlijentu.print("Online korisnici su :");
        for (int i = 0; i < lista.size(); i++) {
            izlazniTokKaKlijentu.print(lista.get(i).ime + ", ");
        }

    }

    public void posaljiPitanje(Pitanje p, int rbrPitanja) throws IOException {
        izlazniTokKaKlijentu.println("SALJEM PITANJE");
        izlazniTokKaKlijentu.println(rbrPitanja + ". " + p.getPitanje());
        izlazniTokKaKlijentu.println(p.getPonudjeniOdgovori()[0]);
        izlazniTokKaKlijentu.println(p.getPonudjeniOdgovori()[1]);
        izlazniTokKaKlijentu.println(p.getPonudjeniOdgovori()[2]);
        izlazniTokKaKlijentu.println(p.getPonudjeniOdgovori()[3]);
        izlazniTokKaKlijentu.println(p.getTacanOdgovor());

    }

    public void posaljiOdgovorProtivniku(ServerNit protivnik) throws IOException {
        odgovorKorisnika = ulazniTokOdKlijenta.readLine();
        protivnik.izlazniTokKaKlijentu.println("PRIMANJE ODGOVORA PROTIVNIKA");
        protivnik.izlazniTokKaKlijentu.println(odgovorKorisnika);

    }

    public void prikaziRezultat() {
        izlazniTokKaKlijentu.println("Rezultat");

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
