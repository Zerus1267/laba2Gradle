package Model;

public class Trip {
    private int id;
    private Country c_out;
    private Country c_in;
    private int min_duration;
    private int min_price;

    public Trip() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Country getC_out() {
        return c_out;
    }

    public void setC_out(Country c_out) {
        this.c_out = c_out;
    }

    public Country getC_in() {
        return c_in;
    }

    public void setC_in(Country c_in) {
        this.c_in = c_in;
    }

    public int getMin_duration() {
        return min_duration;
    }

    public void setMin_duration(int min_duration) {
        this.min_duration = min_duration;
    }

    public int getMin_price() {
        return min_price;
    }

    public void setMin_price(int min_price) {
        this.min_price = min_price;
    }
}
