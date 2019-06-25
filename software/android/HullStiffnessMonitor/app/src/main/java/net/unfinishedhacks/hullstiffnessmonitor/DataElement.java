package net.unfinishedhacks.hullstiffnessmonitor;

public class DataElement implements Comparable<DataElement> {
    private double devAngle10x;
    private double heelAngle;
    private int count;

    public DataElement(double y_elem, double z_elem, double y_dev, double height)
    {
        heelAngle = Math.toDegrees(Math.atan(y_elem/z_elem));
        devAngle10x = Math.toDegrees(Math.atan(y_dev/height));
        count = 1;
        System.out.println("DataElement("+y_elem+","+z_elem+","+y_dev+","+height+") -> heel: "+heelAngle+" dev: "+devAngle10x);
    }

    double getHeelAngleDeg()
    {
        return heelAngle;
    }

    double getDevAngleDeg10x()
    {
        return Math.abs(devAngle10x);
    }

    // Compare heel angles to sort the elements
    @Override
    public int compareTo(DataElement dataElement) {
        double compared = dataElement.getHeelAngleDeg()-heelAngle;
        if (compared < 0.0)
            return -1;
        else if(compared > 0.0)
            return 1;
        else
            return 0;
    }

    public void incrementCount() {
        count = count+1;
    }

    public int getCount() {
        return count;
    }
}
