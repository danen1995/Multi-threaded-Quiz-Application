package kviz;

import java.io.Serializable;

public class Pitanje {

    private String pitanje;
    String[] ponudjeniOdgovori;
    private String tacanOdgovor;

    public Pitanje(String pitanje, String[] ponudjeniOdgovori, String tacanOdgovor) {
        super();
        this.pitanje = pitanje;
        this.ponudjeniOdgovori = ponudjeniOdgovori;
        this.tacanOdgovor = tacanOdgovor;
    }

    public String[] getPonudjeniOdgovori() {
        return ponudjeniOdgovori;
    }

    public void setPonudjeniOdgovori(String[] ponudjeniOdgovori) {
        this.ponudjeniOdgovori = ponudjeniOdgovori;
    }

    public String getTacanOdgovor() {
        return tacanOdgovor;
    }

    public void setTacanOdgovor(String tacanOdgovor) {
        this.tacanOdgovor = tacanOdgovor;
    }

    public Pitanje() {

    }

    public String getPitanje() {
        return pitanje;
    }

    public void setPitanje(String pitanje) {
        this.pitanje = pitanje;
    }

}
