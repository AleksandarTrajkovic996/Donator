package com.arteam.donator;

public class Article extends ArticleID {

    public String tip, naziv, velicina;

    public Article(){
    }

    public Article(String tip, String naziv, String velicina) {
        this.tip = tip;
        this.naziv = naziv;
        this.velicina = velicina;
    }

    public String getTip() {
        return tip;
    }

    public void setTip(String tip) {
        this.tip = tip;
    }

    public String getNaziv() {
        return naziv;
    }

    public void setNaziv(String naziv) {
        this.naziv = naziv;
    }

    public String getVelicina() {
        return velicina;
    }

    public void setVelicina(String velicina) {
        this.velicina = velicina;
    }
}
