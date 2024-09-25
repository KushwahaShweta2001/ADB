package utils;

public class Object {  
	
	public int[] position ;
	public double[] volume;
	public double[] density;
	public double[] acceleration;
	public double[] acc_norm;
    public double fitness;
    public boolean feasibility;
    public double cost;
    public double makespan;
    
	
        
    public Object(int dim) {
	  this.position = new int[dim];
	  this.volume = new double[dim];
	  this.density = new double[dim];
	  this.acceleration = new double[dim];
	  this.acc_norm = new double[dim];
	  this.fitness=-1;
	  this.feasibility=false;
	  this.cost=0.0;
	  this.makespan=0.0;
        
    }
}