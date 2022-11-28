package com.moovapps.gp.budget.helpers;

public class Const {


    // Préparation du budget
    public static String ACTION_ANNULER_PB = "Annuler";

    public static String ACTION_ENVOYER_VALIDATION_PB = "EnvoyerPourValidation";

    public static String ACTION_ACCEPTER_PB = "Accepter";

    public static String ACTION_REFUSER_PB = "Refuser";

    public static String ACTION_RETOUR_MODIFICATION_PB = "RetourPourModification";


    // Génération du budget
    public static String ACTION_ENVOYER_VALIDATION_GB = "EnvoyerPourValidation";

    public static String ACTION_ACCEPTER_GB = "Accepter";

    public static String ACTION_REFUSER_GB = "Refuser";

    public static String ACTION_RETOUR_MODIFICATION_GB = "RetourPourModification";

    public static String ACTION_RETOUR_MODIFICATION2_GB = "RetourPourModification2";

    public static String ACTION_GENERER_VERSION_GB = "GenererUneNouvelleVersion";

    public static String ACTION_BUDGET_REFUSER_GB = "BudgetRefuse";

    public static String ACTION_BUDGET_VALIDER_GB = "BudgetValide";

    public static String ACTION_CLOTURER_BUDGET_GB = "CloturerLeBudget";




    public static enum NatureBudget {
        Investissement,
        Fonctionnement,
        Autre;
        private NatureBudget() {
        }
    }


    public static enum Properties {
        AnneeBudgetaire,
        TypeBudget,
        NatureBudget,
        PB_Budget_Tab,
        ChargementDesRubriques;
        private Properties() {
        }
    }

}
