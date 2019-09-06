package kviz;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.util.LinkedList;
import java.util.Random;

public class GrupaPitanja {

    private static LinkedList<Pitanje> pitanja = new LinkedList<>();

    @Override
    public String toString() {
        return "GrupaPitanja [pitanja=" + pitanja + "]";
    }

    public LinkedList<Pitanje> getPitanja() {
        return pitanja;
    }

    public void setPitanja(LinkedList<Pitanje> pitanja) {
        this.pitanja = pitanja;
    }

    public void unesiPitanje(Pitanje pitanje) {
        pitanja.add(pitanje);
    }

    public int ukupanBrojPitanja() {
        return pitanja.size();
    }

}
