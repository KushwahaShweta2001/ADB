package planning;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.Consts;
import org.cloudbus.cloudsim.Datacenter;
import org.cloudbus.cloudsim.DatacenterBroker;
import org.cloudbus.cloudsim.DatacenterCharacteristics;
import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Pe;
import org.cloudbus.cloudsim.Storage;
import org.cloudbus.cloudsim.UtilizationModel;
import org.cloudbus.cloudsim.VmAllocationPolicySimple;
import org.cloudbus.cloudsim.VmSchedulerTimeShared;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.provisioners.BwProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.PeProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.RamProvisionerSimple;
import org.workflowsim.CondorVM;
import org.workflowsim.Task;

import utils.Event;
import utils.Object;
import utils.Parameters;
import utils.Schedule;
import vm.CustomVM;
import vm.CustomVMGenerator;




public abstract class AOA_Population extends BasePlanning{
	
  public int dim; 
	public int numOfObjects=50;
	public int epochs=100;
	public double w=0.5;
	public double c1=1;
	public double c2=6;
	public double c3=2;
	public double c4=1;
	public double u=0.9;
	public double l=0.1;
//	public int VMs = getVmList().size(); 
	public Object[] population= new Object[numOfObjects];
	double max_time = 1000000;       
	double max_cost= 50000;
    public Object gbest;
    double B_min = 0.2; 
    double B_max = 1.2;
    public  Object[] random_soln= new Object[numOfObjects];
    private  List<Cloudlet> cloudletList;
    public double alpha =0.4 ;
 

    public void create_population() {
    	
    	dim= getTaskList().size();
    	gbest = new Object(dim);
        gbest.fitness = 0.0;
        
        for (int i = 0; i < numOfObjects; i++) {
//        	System.out.println("Object "+i);
            Object c_rand = new Object(dim);
            generate(c_rand);
            population[i] = c_rand;
           
            
            if (gbest.fitness < c_rand.fitness) {
                gbest = c_rand; } 		//*-*-*-*-*-*-**-*-*----*-------------**********-------------*-*-*-*--*--*-
        }
    }    
    
    
    
    public void generate(Object Object ) {
        Random random = new Random();        
        while (!Object.feasibility) {
            for (int i = 0; i < dim; i++) {
            	random = new Random();
               Object.position[i] = random.nextInt(getVmList().size()-1);
              

               Random ran = new Random(); 
               Random ran1 = new Random(); 
               Random ran2 = new Random(); 
               Random ran3 = new Random(); 
               
               Object.volume[i]= ran.nextDouble();
               Object.density[i]= ran1.nextDouble();
               Object.acceleration[i]= ran2.nextDouble();
               Object.acc_norm[i]= ran3.nextDouble();
//               System.out.println("Task: "+ i+ "	VM: "+ Object.position[i] + "	Volume: " + Object.volume[i] + "	Density: " +Object.density[i] + "	Acceleration: "+ Object.acceleration[i]+ "	Acc Norm: "+ Object.acc_norm[i]);
//               System.out.println();
            }
            
            Schedule schedule_Object = generate_schedule(Object);
            int z=0;
            for(Task task:getTaskList()) {
            	task.setVmId(Object.position[z]);
            	z++;
            }
            List<Cloudlet> jobs=exec(getTaskList());
            double makespan= 0.0;
            makespan=getMKSP(jobs);
        	double cost=0.0;
        	cost=getCost(getVmList(),jobs);
            
            if(schedule_Object.TET<=max_time) {
            	Object.feasibility=true;
            	double nzm,nzc,pw;
                
            	nzm=makespan/max_time;
            	nzc=cost/max_cost;
          
            	pw=Math.max(1.0,(makespan/Example_DB.Deadline)*(makespan/Example_DB.Deadline))*Math.max(1.0,(cost/Example_DB.Budget)*(cost/Example_DB.Budget));
            	Object.fitness=1/((alpha*nzm+(1-alpha)*nzc)*pw);
            	if(makespan>max_time) {max_time=makespan;};
            	if(cost>max_cost) {max_cost=cost;}
            	Object.cost=schedule_Object.TEC;
            	Object.makespan=schedule_Object.TET;
//            	 System.out.println("	Feasibility: "+ Object.feasibility + "	Fitness: " + Object.fitness + "	Cost: " +Object.cost + "	Makespan: "+ Object.makespan);
//                 System.out.println();
            }
          
        }      
        
      }
    
    public Schedule generate_schedule(Object Object) {
    	Schedule schedule =new Schedule();
    	
    	int p_size=getTaskList().size();    	
    	List<Task> tasklist=getTaskList();
    	
    	Map<Task, Double> ET = new HashMap<>();
    	Map<Integer, Double> LET = new HashMap<>();
    	Map<Integer, Double> LST = new HashMap<>();
    	
    	//----------    	
    	Map <Task,Integer> task_no = new HashMap<>();
    	
    	int k=0;
    	for(Task task:getTaskList()) {
    		task_no.put(task, k);
    		k++;
    	}
    	
    	for(CustomVM vm: getVmList()) {
    		LET.put(vm.getId(), 0.0);
    		LST.put(vm.getId(), 0.0);
    	}
    	
    	for(Task task: getTaskList()) {
    		ET.put(task, 0.0);
    		
    	}
    	
    	double boot_time=0.0;
    	
    	for(int i=0;i<p_size;i++) {
    		Task t=tasklist.get(i);
    		double start_time = 0.0;
    		
    		if(t.getParentList().size() == 0)
    		{
    			start_time = LET.get(Object.position[i]);
    		}
    		else
    		{
    			double max_ET = 0.0;
    			for(Task parent_task : t.getParentList()) {
    				max_ET = Math.max(max_ET, ET.get(parent_task));
    			}
    			
    			//Log.printLine(Object.position[i]);
//    			Log.printLine(LET.get(2));
    			
    			start_time = Math.max(LET.get(Object.position[i]), max_ET);
    		}
    		double exec= computationCosts.get(t).get(Object.position[i]);
    		double transfer = 0.0;
    		
    		for(Task child_task : t.getChildList())
    		{
    			if(Object.position[task_no.get(child_task)] != Object.position[i]) {
    				transfer += transferCosts.get(t).get(child_task);
    			}
    		}
    		
    		ET.remove(t);
    		ET.put(t, exec+transfer+start_time);
    		
    		Event event =new Event(t, Object.position[i], start_time, ET.get(t));
    		
    		schedule.mappings.add(event);
    		
    		
    		if(!schedule.resources.contains(Object.position[i])){
    			LST.put(Object.position[i],Math.max(start_time, boot_time));
    			schedule.resources.add(Object.position[i]);
    		}
    		
    		LET.put(Object.position[i],exec+transfer+Math.max(Math.max(start_time, boot_time),LET.get(Object.position[i])));
    		
    		
    	}
    	
    	
    	for(Task task1:getTaskList()) {
    		schedule.TET=Math.max(schedule.TET, ET.get(task1));    		
    	}
    	double starting=Double.MAX_VALUE;
    	
    	int x=0;
    	for(Task task1:getTaskList()) {
    		starting=Math.min(schedule.mappings.get(x).start_time, starting);
    		x++;
    	}
    	schedule.TET-=starting;
    	
    	for(int i=0;i<schedule.resources.size();i++) {
    		schedule.TEC+=(getVmList().get(schedule.resources.get(i)).getCost())*(LET.get(getVmList().get(schedule.resources.get(i)).getId())-LST.get(getVmList().get(schedule.resources.get(i)).getId()));  		
    	}
    	return schedule;
    }
    
    
    
 
    
//    public void updatevolumeFirst (Object Object,int Object_no) {
//    //	System.out.println("---------------------------Updating VOLUME-------------------------");
//    		for(int i=0;i<dim;i++) {
//        	Random random = new Random();
//            double r = random.nextDouble();
//            Object.volume[i]+=r+(gbest.volume[i]-Object.volume[i]);
//        
//    	}
//       
//    }
    
   
   
    public double[] updatevolume (Object Object,int Object_no) {
    	//System.out.println("---------------------------Updating VOLUME-------------------------");
    		for(int i=0;i<dim;i++) {
        	Random random = new Random();
            double r = random.nextDouble();
            Object.volume[i]+=r*(gbest.volume[i]-Object.volume[i]);
        
    	}
			return Object.volume;
       
    }
    
public double[] updateAccBeg (Object Object, int Object_no, Object Random_Object) {
//	System.out.println("---------------------------UPDATE ACC BEG-------------------------");
    	for(int i=0;i<dim;i++) {
        Object.acceleration[i]=( Random_Object.density[i]+ Random_Object.volume[i]*Random_Object.acceleration[i])/(Object.density[i]*Object.volume[i]);
       }
		return Object.acceleration;
    	}
    


 public double[] updateAccNormBeg (Object Object,int Object_no, double ACC_min, double ACC_max) {
    	        
    	    	for(int i=0;i<dim;i++) {
    	    	
    	        Object.acc_norm[i]+=(u*(Object.acceleration[i]-ACC_min)/(ACC_max-ACC_min))+l;
    	      }
    	    	return Object.acc_norm;}
 
 public int[] updatePositionBeg (Object Object,int Object_no,Object Random_Object, double density_factor) {
	
 	for(int i=0;i<dim;i++) {
 		Random random = new Random();
        double r = random.nextDouble();
     Object.position[i]= (int) (Object.position[i]+ c1 *r* Object.acc_norm[i] *density_factor*(Random_Object.position[i]-Object.position[i]));
     if(Object.position[i]<=0) {
    	 Random random1 = new Random();
			int r1 = random1.nextInt(getVmList().size()-1);
			Object.position[i]=r1;
     }
     int VMs=getVmList().size();
     if(Object.position[i]>=VMs) {
   	  Object.position[i]=Object.position[i]%VMs;
   	  
     } 
//     if(Object.position[i]<0) {
//     	 Object.position[i]=0;
//      }
//      int vms=getVmList().size();
//      if(Object.position[i]>=vms) {
//     	 Object.position[i]=vms-1;
//      }
    }
	return Object.position;
 	}
 
 
 
 
 
 
 
 
 
 
 public double[] updateAccEnd (Object Object,int Object_no) {
     
 	for(int i=0;i<dim;i++) {
     Object.acceleration[i]=( gbest.density[i]+ gbest.volume[i]*gbest.acceleration[i])/(Object.density[i]*Object.volume[i]);
    }
 	return Object.acceleration;}
 


public double[] updateAccNormEnd (Object Object,int Object_no, double ACC_min, double ACC_max) {
 	        
 	    	for(int i=0;i<dim;i++) {
 	    	
 	        Object.acc_norm[i]+=(u*(Object.acceleration[i]-ACC_min)/(ACC_max-ACC_min))+l;
 	      }
 	    	return Object.acc_norm;}

public int[] updatePositionEnd (Object Object,int Object_no, double density_factor, double transfer_factor, int flag) {
  
	for(int i=0;i<dim;i++) {
		Random random = new Random();
     double r = random.nextDouble();
  Object.position[i]=(int) (gbest.position[i]+ flag * c2 *r* Object.acc_norm[i] *density_factor*(transfer_factor*c3 * gbest.position[i]-Object.position[i]));
  if(Object.position[i]<=0) {
 	 Random random1 = new Random();
			int r1 = random1.nextInt( getVmList().size()-1);
			Object.position[i]=r1;
  }
  int VMs=getVmList().size();
  if(Object.position[i]>=VMs) {
	  Object.position[i]=Object.position[i]%VMs;
	  
  } 
  
//  if(Object.position[i]<0) {
// 	 Object.position[i]=0;
//  }

//  if(Object.position[i]>=vms) {
// 	 Object.position[i]=vms-1;
//  }
	}
	return Object.position;
	}
 
    	
    	  public double[] updatedensity (Object Object,int Object_no) {
    		//  System.out.println("--------------------------- UPDATE DENSITY-------------------------");
    	    
    	    	for(int i=0;i<dim;i++) {
        	    	Random random = new Random();
        	        double r = random.nextDouble();
        	        Object.density[i]+=r*(gbest.density[i]-Object.density[i]);
        	       
        	          	    	}
				return Object.density;
    	    	
       
    }
//    	  public void updatedensityFirst (Object Object,int Object_no) {
//    		 // System.out.println("--------------------------- UPDATE DENSITY-------------------------");
//    	    
//    	    	for(int i=0;i<dim;i++) {
//        	    	Random random = new Random();
//        	        double r = random.nextDouble();
//        	        Object.density[i]+=r+(gbest.density[i]-Object.density[i]);
//        	        System.out.println("Density of "+i+"th task is "+ Object.density[i]);
//        	          	    	} }
//    
 
    
 public void update_gbest() {
        
    	for(int i=0;i<numOfObjects;i++) {  
        
    		if(gbest.makespan<population[i].makespan) {
    			gbest=population[i];
    		}
       
    	}
       
    }
    
    
 public List<Cloudlet>  exec(List<Task> tasklist) {
 	List<Cloudlet> newList=new ArrayList<>();
		Log.printLine("Starting Hi CloudSim...");

		try {
			 	int num_user = 1; // number of cloud users
			 	Calendar calendar = Calendar.getInstance();
			 	boolean trace_flag = false; // mean trace events
			 	CloudSim.init(num_user, calendar, trace_flag);
			
			 	//*************	DATACENTER CREATION 	**************
			 	
			 	List<Host> hostList = new ArrayList<Host>();
			 	List<Pe> peList = new ArrayList<Pe>();
			 	int mips = 1000*12;//reqvms
			 	peList.add(new Pe(0, new PeProvisionerSimple(mips))); // need to store Pe id and MIPS Rating
			 	int hostId=0;
			 	int ram = 12288; //host memory (MB)
			 	long storage = 1000000; //host storage
			 	int bw = 10000;
			 	hostList.add(
	    			new Host(
	    				hostId,
	    				new RamProvisionerSimple(ram),
	    				new BwProvisionerSimple(bw),
	    				storage,
	    				peList,
	    				new VmSchedulerTimeShared(peList)
	    			)
	    		); 

			String arch = "x86";      // system architecture
	        String os = "Linux";          // operating system
	        String vmm = "Xen";
	        double time_zone = 10.0;         // time zone this resource located
	        double cost = 3.0;              // the cost of using processing in this resource
	        double costPerMem = 0.05;		// the cost of using memory in this resource
	        double costPerStorage = 0.001;	// the cost of using storage in this resource
	        double costPerBw = 0.0;			// the cost of using bw in this resource
	        LinkedList<Storage> storageList = new LinkedList<Storage>();	//we are not adding SAN devices by now

	        DatacenterCharacteristics characteristics = new DatacenterCharacteristics(
	                arch, os, vmm, hostList, time_zone, cost, costPerMem, costPerStorage, costPerBw);


	        // 6. Finally, we need to create a PowerDatacenter object.
	        Datacenter datacenter = null;
	        try {
	            datacenter = new Datacenter("Datacenter_0", characteristics, new VmAllocationPolicySimple(hostList), storageList, 0);
	        } catch (Exception e) {
	            e.printStackTrace();
	        }

	        System.out.println("Success!! DatacenterCreator is executed!!");
	       
			DatacenterBroker broker = createBroker();
			int brokerId = broker.getId();
			
			List<CustomVM> vmlist0 = CustomVMGenerator.createCustomVMs(
					brokerId,
                 utils.Parameters.num_vms
         );
			
			broker.submitVmList(vmlist0);
			
			
			cloudletList = new ArrayList<Cloudlet>();
			
			for(Task task:tasklist) {
			int id = task.getCloudletId();
			long length = task.getCloudletLength();
			long fileSize = task.getCloudletFileSize();
			long outputSize = task.getCloudletOutputSize();
			UtilizationModel utilizationModel = task.getUtilizationModelCpu();
			int pesNumber=task.getNumberOfPes();
			Cloudlet cloudlet = new Cloudlet(id, length, pesNumber, fileSize, outputSize, utilizationModel, utilizationModel, utilizationModel);
			cloudlet.setUserId(brokerId);
			cloudlet.setVmId(task.getVmId());
			cloudletList.add(cloudlet);
			}
      
			broker.submitCloudletList(cloudletList);
			
			CloudSim.startSimulation();
			CloudSim.stopSimulation();
			
			newList = broker.getCloudletReceivedList();
			Log.printLine("Hi CloudSim finished!");
		} catch (Exception e) {
			e.printStackTrace();
			Log.printLine("Unwanted errors happen");
			
		}
		
		return newList;
	}

 
//******************************** 	CREATE BROKER 	******************************************
 
 
 private  DatacenterBroker createBroker() {
												DatacenterBroker broker = null;
												try { broker = new DatacenterBroker("Broker"); }
												catch (Exception e) {e.printStackTrace();
												return null; }
												return broker;
											 }

 public static double getMKSP(List<Cloudlet> jobs) {
     double start = Double.MAX_VALUE, end = Double.MIN_VALUE;
     for( Cloudlet j:jobs) {
         start = Math.min(start, j.getExecStartTime());
         end = Math.max(end, j.getFinishTime());
     }
     return end - start;
 }
 public  double getCost(List<CustomVM> vms, List<Cloudlet> jobs) {
     double cost = 0;
     for(Cloudlet j: jobs) {
         CondorVM vm = null;
         for(CondorVM cvm:vms) {
             if(cvm.getId() == j.getVmId()) {
                 vm = cvm;
                 break;
             }
         }
         assert vm != null;

         //cost for execution on vm
         cost += j.getActualCPUTime() * vm.getCost();
         
         //cost for file transfer on this vm
         long fileSize = (j.getCloudletFileSize()+j.getCloudletOutputSize()) / Consts.MILLION;
         
         cost += vm.getCostPerBW() * fileSize;
     }
     return cost;
 }
 
 protected void performLEO(Object Object ,Object gbest, Object object_1, Object object_2,  Object object_3, Object[] random_soln, int i) {
		int L1;
		double u1, u2, u3, B, A, phi;
		Random random = new Random();
		 Object random_1, random_2, new_random;
		random_1= random_soln[0];
		random_2= random_soln[1];
		

	        // Generate two random numbers between -1 and 1
	        double f1 = -1 + 2 * random.nextDouble();
	        double f2 = -1 + 2 * random.nextDouble();
	        double mew1 = random.nextDouble();
	        double rand = random.nextDouble();
		if(mew1<0.5) {L1=1;}
		else {L1=0;}
		
		u1= L1 * 2 * rand + (1-L1);
		u2= L1 * rand + (1-L1);
		u3= L1 * rand + (1-L1);
		new_random =  random_soln[2];
		
		
		B= B_min + (B_max- B_min) * (Math.pow(1-Math.pow((i/epochs),3),2));
		A= Math.abs(B*Math.sin((3*Math.PI/2)+Math.sin(B * (3* Math.PI/2)) ));
		phi =2 * rand*A-A;
		 
		 double r2 = random.nextDouble();
		if(r2<0.4) {
			for(int p=0;p<dim;p++) {
				 Object.position[p]= (int) (Object.position[p]+f1*(u1*gbest.position[p]-u2*(L1* object_3.position[p] +(1-L1)*new_random.position[p]))+f2*phi*(u3*(random_2.position[p]-random_1.position[p]))+u2*(object_1.position[p]-object_2.position[p])/2);
				
				 if(Object.position[p]<=0) {
				 	 Random random1 = new Random();
							int r1 = random1.nextInt( getVmList().size()-1);
							Object.position[p]=r1;
				  }
				 int VMs=getVmList().size();
				  if(Object.position[p]>=VMs) {
					  Object.position[p]=Object.position[p]%VMs;
					  
				  } 
				  
			}} 
		else {
			for(int p=0;p<dim;p++) {
				Object.position[p]= (int) (gbest.position[p]+f1*(u1*gbest.position[p]-u2*(L1* object_3.position[p] +(1-L1)*new_random.position[p]))+f2*phi*(u3*(random_2.position[p]-random_1.position[p]))+u2*(object_1.position[p]-object_2.position[p])/2);
				
				 if(Object.position[p]<=0) {
				 	 Random random1 = new Random();
							int r1 = random1.nextInt( getVmList().size()-1);
							Object.position[p]=r1;
				  }
				 int VMs=getVmList().size();
				  if(Object.position[p]>=VMs) {
					  Object.position[p]=Object.position[p]%VMs;
					  
				  } 
		}}
			
		}  
 public void create_random_solutions(int number) {
 	
 	dim= getTaskList().size();
 	gbest = new Object(dim);
     gbest.fitness = 0.0;
     
     for (int i = 0; i < number; i++) {
     	Object c_rand = new Object(dim);
         generate(c_rand);
         random_soln[i] = c_rand;
        
 }
 }
   
}
