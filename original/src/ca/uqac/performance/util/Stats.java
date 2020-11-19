package ca.uqac.performance.util;

public class Stats {
    private int count;
    private int tot = 0;
    private int min = 9999999;
    private int max = 0;
    private int cost = 1;

    public Stats(){

    }

    public Stats(Integer cost){
        this.cost = cost;
    }

    public void calc(int value){
        count++;
        tot += value;
        min = Math.min(value, min);
        max = Math.max(value, max);
    }

    public int tot(){
        return tot;
    }

    public int totCost(){
        return tot * cost;
    }

    public int min(){
        return min;
    }

    public int minCost(){
        return min * cost;
    }

    public int max(){
        return max;
    }

    public int maxCost(){
        return max * cost;
    }

    public float avg(){
        return ((float) tot) / count;
    }

    public float avgCost(){
        return ((float) tot * cost) / count;
    }

    public static String format(int number){
        return String.format("%" + 10 + "s", number);
    }

    public static String format(float number){
        return String.format("%" + 10 + "s", number);
    }
}
