package com.moovapps.gp.helpers;

public class ConvertirMontantEnLettre {
    public static final String ZERO = "zéro";

    public static final String UN = "un";

    public static final String DEUX = "deux";

    public static final String TROIS = "trois";

    public static final String QUATRE = "quatre";

    public static final String CINQ = "cinq";

    public static final String SIX = "six";

    public static final String SEPT = "sept";

    public static final String HUIT = "huit";

    public static final String NEUF = "neuf";

    public static final String DIX = "dix";

    public static final String ONZE = "onze";

    public static final String DOUZE = "douze";

    public static final String TREIZE = "treize";

    public static final String QUATORZE = "quatorze";

    public static final String QUINZE = "quinze";

    public static final String SEIZE = "seize";

    public static final String VINGT = "vingt";

    public static final String TRENTE = "trente";

    public static final String QUARANTE = "quarante";

    public static final String CINQUANTE = "cinquante";

    public static final String SOIXANTE = "soixante";

    public static final String CENT = "cent";

    public static final String CENTS = "cents";

    public static final String MILLE = "mille";

    public static final String MILLION = "million";

    public static final String MILLIARD = "milliard";

    public static final String MOINS = "moins";

    private static final String[] tab = new String[] { "", "mille", "million", "milliard", "mille milliard", "million de milliard", "milliard de milliard" };

    public static String getChiffre(int l) {
        if (l < 0 || l > 9)
            throw new IllegalArgumentException("Un chiffre est entre 0 et 9, donc " + l + " est interdit");
        switch (l) {
            case 0:
                return "zéro";
            case 1:
                return "un";
            case 2:
                return "deux";
            case 3:
                return "trois";
            case 4:
                return "quatre";
            case 5:
                return "cinq";
            case 6:
                return "six";
            case 7:
                return "sept";
            case 8:
                return "huit";
            case 9:
                return "neuf";
        }
        return null;
    }

    private static String paquet(int p) {
        String reponse = "";
        if (p >= 100) {
            if (p / 100 > 1)
                reponse = String.valueOf(getChiffre(p / 100)) + " ";
            if (p / 100 > 1) {
                reponse = String.valueOf(reponse) + "cent ";
            } else {
                reponse = String.valueOf(reponse) + "cent ";
            }
            p %= 100;
        }
        int c = p / 10;
        int u = p % 10;
        switch (c) {
            case 0:
                if (u != 0)
                    reponse = String.valueOf(reponse) + getChiffre(u);
                break;
            case 1:
                switch (u) {
                    case 0:
                        reponse = String.valueOf(reponse) + "dix";
                        break;
                    case 1:
                        reponse = String.valueOf(reponse) + "onze";
                        break;
                    case 2:
                        reponse = String.valueOf(reponse) + "douze";
                        break;
                    case 3:
                        reponse = String.valueOf(reponse) + "treize";
                        break;
                    case 4:
                        reponse = String.valueOf(reponse) + "quatorze";
                        break;
                    case 5:
                        reponse = String.valueOf(reponse) + "quinze";
                        break;
                    case 6:
                        reponse = String.valueOf(reponse) + "seize";
                        break;
                }
                reponse = String.valueOf(reponse) + "dix " + getChiffre(u);
                break;
            case 2:
                reponse = String.valueOf(reponse) + "vingt";
                if (u == 1)
                    reponse = String.valueOf(reponse) + " et";
                if (u > 0)
                    reponse = String.valueOf(reponse) + " " + getChiffre(u);
                break;
            case 3:
                reponse = String.valueOf(reponse) + "trente";
                if (u == 1)
                    reponse = String.valueOf(reponse) + " et";
                if (u > 0)
                    reponse = String.valueOf(reponse) + " " + getChiffre(u);
                break;
            case 4:
                reponse = String.valueOf(reponse) + "quarante";
                if (u == 1)
                    reponse = String.valueOf(reponse) + " et";
                if (u > 0)
                    reponse = String.valueOf(reponse) + " " + getChiffre(u);
                break;
            case 5:
                reponse = String.valueOf(reponse) + "cinquante";
                if (u == 1)
                    reponse = String.valueOf(reponse) + " et";
                if (u > 0)
                    reponse = String.valueOf(reponse) + " " + getChiffre(u);
                break;
            case 6:
                reponse = String.valueOf(reponse) + "soixante";
                if (u == 1)
                    reponse = String.valueOf(reponse) + " et";
                if (u > 0)
                    reponse = String.valueOf(reponse) + " " + getChiffre(u);
                break;
            case 7:
                reponse = String.valueOf(reponse) + "soixante ";
                if (u == 1)
                    reponse = String.valueOf(reponse) + " et";
                switch (u) {
                    case 0:
                        reponse = String.valueOf(reponse) + "dix";
                        break;
                    case 1:
                        reponse = String.valueOf(reponse) + "onze";
                        break;
                    case 2:
                        reponse = String.valueOf(reponse) + "douze";
                        break;
                    case 3:
                        reponse = String.valueOf(reponse) + "treize";
                        break;
                    case 4:
                        reponse = String.valueOf(reponse) + "quatorze";
                        break;
                    case 5:
                        reponse = String.valueOf(reponse) + "quinze";
                        break;
                    case 6:
                        reponse = String.valueOf(reponse) + "seize";
                        break;
                }
                reponse = String.valueOf(reponse) + "dix " + getChiffre(u);
                break;
            case 8:
                reponse = String.valueOf(reponse) + "quatre-vingt";
                if (u > 0)
                    reponse = String.valueOf(reponse) + " " + getChiffre(u);
                break;
            case 9:
                reponse = String.valueOf(reponse) + "quatre-vingt ";
                switch (u) {
                    case 0:
                        reponse = String.valueOf(reponse) + "dix";
                        break;
                    case 1:
                        reponse = String.valueOf(reponse) + "onze";
                        break;
                    case 2:
                        reponse = String.valueOf(reponse) + "douze";
                        break;
                    case 3:
                        reponse = String.valueOf(reponse) + "treize";
                        break;
                    case 4:
                        reponse = String.valueOf(reponse) + "quatorze";
                        break;
                    case 5:
                        reponse = String.valueOf(reponse) + "quinze";
                        break;
                    case 6:
                        reponse = String.valueOf(reponse) + "seize";
                        break;
                }
                reponse = String.valueOf(reponse) + "dix-" + getChiffre(u);
                break;
        }
        return reponse.trim();
    }

    public static String getLettre(long l) {
        if (l == 0L)
            return "zéro";
        String signe = "";
        if (l < 0L) {
            l = -l;
            signe = "moins ";
        }
        String reponse = "";
        int rang = 0;
        while (l > 0L) {
            if ((int)(l % 1000L) > 0) {
                reponse = String.valueOf(paquet((int)(l % 1000L))) + " " + tab[rang] + " " + reponse;
            } else {
                reponse = String.valueOf(paquet((int)(l % 1000L))) + " " + reponse;
            }
            l /= 1000L;
            rang++;
        }
        reponse = String.valueOf(signe) + reponse;
        return reponse.trim();
    }

    public static String getLettre(int l) {
        if (l == 0)
            return "zéro";
        String signe = "";
        if (l < 0) {
            l = -l;
            signe = "moins ";
        }
        String reponse = "";
        int rang = 0;
        while (l > 0L) {
            reponse = String.valueOf(paquet(l % 1000)) + " " + tab[rang] + " " + reponse;
            l /= 1000;
            rang++;
        }
        reponse = String.valueOf(signe) + reponse;
        return reponse.trim();
    }

    public static void main(String[] args) {
        ConvertirMontantEnLettre nombre1 = new ConvertirMontantEnLettre();
    }

    public static String begin(String nbre) {
        String txt = "";
        String valx = "";
        String valy = "";
        int x = 0;
        String nbr = nbre;
        x = nbr.indexOf(".");
        valx = nbr.substring(0, x);
        valy = nbr.substring(x + 1, nbr.length());
        String xx = valy;
        if (xx.length() == 1)
            valy = String.valueOf(valy) + "0";
        long ax = Long.parseLong(valx);
        int ay = Integer.parseInt(valy);
        if (x != -1) {
            txt = String.valueOf(getLettre(ax)) + " Dirhams et " + getLettre(ay) + " Centimes";
            if (txt.contains("zCentimes"))
                txt = txt.replace("et zCentimes", "");
        } else {
            txt = String.valueOf(getLettre(ax)) + " Dirhams ";
        }
        if (txt.toUpperCase().startsWith("UN") && !txt.toUpperCase().startsWith("UN MILLION"))
            txt = txt.replaceFirst("UN", "");
        return txt.toUpperCase();
    }
}
