package com.moovapps.gp.budget.helpers;

public class calculate {

    public static double RB_calculateCurrentRAP(double totalEngagement , double totalPaiement , double rapLibere , double totalDiminution){
        return (totalEngagement - totalPaiement - rapLibere - totalDiminution);
    }

}
