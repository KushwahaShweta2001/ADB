  package planning;


import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Random;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.Log;
import org.workflowsim.Task;

import utils.AOA_Metrics;
import utils.AOA_Metrics;
import utils.Parameters;
import utils.Schedule;




public class AOA_DB extends AOA_Metrics {

   private static final double MAX = 1000000000;
   private static final double MIN=-1000000000;
   public double alpha =0.4 ;
double max_time = 1000000;
double max_cost= 50000;

    @Override
    public void run() throws InvocationTargetException {
    	 System.out.println("Deadline	"+ Example_DB.Deadline);
		 System.out.println("Budget	"+ Example_DB.Budget);
    	for (int t = 0; t < Parameters.simulations; t++) {  
        Log.printLine("AOA planner running with " + getTaskList().size()
                + " tasks and " + getVmList().size() + "vms for the simulation  "+ t );
        
        averageBandwidth = calculateAverageBandwidth();
        calculateComputationCosts();
        calculateTransferCosts();
        System.out.println("--------------------------- Population Generated -------------------------");
        create_population();
      
        for (int i = 0; i < epochs; i++) {
        	
//        	System.out.println("--------------------------- Epoch " + i +"-------------------------");
        	 double random3 = (double)(-i +epochs)/epochs;
             double random2 = (double)i/epochs;
             double random1 = (double)(i -epochs)/epochs;
				double transfer_factor = (double)(Math.exp(random1));
				double density_factor = (double)(Math.exp((random3 - random2)));
//				System.out.println("Transfer Factor & Density Factor Calculated");
//				System.out.println("Transfer Factor : " + transfer_factor);
//				System.out.println("Density Factor : " + density_factor);
				
        	for(int j=0;j<numOfObjects; j++) {
//        		System.out.println("---------------Object " + j + "---------------");
//        		double[] Densities = new double[dim];
// 				Densities = 
 						updatedensity(population[j],j);
// 				for(int i1 = 0; i1<dim;i1++) {
// 					System.out.println("The density of " + i1 + "th task is "+ Densities[i1]);
// 				}
//            
// 				
// 				double[] Volumes = new double[dim];
// 				Volumes = 
 						updatevolume(population[j],j);
// 				for(int i1 = 0; i1<dim;i1++) {
// 					System.out.println("The volume of " + i1 + "th task is "+ Volumes[i1]);
// 				}
           	
            	
 				if(transfer_factor<=0.5) {
 				//Exploration Phase
 					//System.out.println("---------------Exploration Phase---------------");
 					Random random = new Random();
 					int r = random.nextInt( numOfObjects- 1) + 1;
 			       double[] ACC = new double[dim];
	 			ACC=updateAccBeg(population[j],j,population[r]);
//	 			for(int i1 = 0; i1<dim;i1++) {
//					System.out.println("The Acceleration of " + i1 + "th task is "+ ACC[i1]);
//				}
 				double ACC_min = findACC_min(ACC);
 				double ACC_max = findACC_max(ACC);
// 				 double[] ACC_Norm = new double[dim];
// 	 			ACC_Norm = 
 	 					updateAccNormBeg(population[j],j, ACC_min, ACC_max);
// 	 			for(int i1 = 0; i1<dim;i1++) {
// 					System.out.println("The Normalized Acceleration of " + i1 + "th task is "+ ACC_Norm[i1]);
// 				}
// 				int[] Allocation = new int[dim];
// 				Allocation = 
 						updatePositionBeg(population[j],j,population[r],density_factor);
// 				for(int i1 = 0; i1<dim;i1++) {
// 					System.out.println("The allocation of " + i1 + "th task is "+ Allocation[i1]);
// 				}
 			}
 				
 				else {
 					
 					// Exploitation Phase
 					//System.out.println("---------------Exploitation Phase---------------");
 					
 					  double[] ACC = new double[dim];
 			 			ACC=updateAccEnd(population[j],j);
// 			 			for(int i1 = 0; i1<dim;i1++) {
// 							System.out.println("The Acceleration of " + i1 + "th task is "+ ACC[i1]);
// 						}
 		 				double ACC_min = findACC_min(ACC);
 		 				double ACC_max = findACC_max(ACC);
// 		 				 double[] ACC_Norm = new double[dim];
// 		 	 			ACC_Norm = 
 		 	 					updateAccNormEnd(population[j],j, ACC_min, ACC_max);
// 		 	 			for(int i1 = 0; i1<dim;i1++) {
// 		 					System.out.println("The Normalized Acceleration of " + i1 + "th task is "+ ACC_Norm[i1]);
// 		 				}
 					
 	 				
 	 				int flag=0;
 	 				double P =0.5;
 	 				Random random = new Random();
 			        int r = random.nextInt();
 	 				 P=2*r-c4;
 	 				 if(P<=0.5) {flag = 1;}
 	 				 else {flag = -1;}
 	 				 

 	 						updatePositionEnd(population[j],j,density_factor, transfer_factor,flag);
 			
 				}
 				
            	Schedule schedule_Object = generate_schedule(population[j]);
            	 int z=0;
                 for(Task task:getTaskList()) {
                 	task.setVmId(population[j].position[z]);
                 	z++;
                 }
                 List<Cloudlet> jobs=exec(getTaskList());
                 double makespan= 0.0;
                 makespan=getMKSP(jobs);
             	double cost=0.0;
             	cost=getCost(getVmList(),jobs);
                if(schedule_Object.TET<=max_time) {
                	population[j].feasibility=true;
                	double nzm,nzc,pw;
                
                	nzm=makespan/max_time;
                	nzc=cost/max_cost;
              
                	pw=Math.max(1.0,(makespan/Example_DB.Deadline)*(makespan/Example_DB.Deadline))*Math.max(1.0,(cost/Example_DB.Budget)*(cost/Example_DB.Budget));
                	population[j].fitness=1/((alpha*nzm+(1-alpha)*nzc)*pw);
                	if(makespan>max_time) {max_time=makespan;};
                	if(cost>max_cost) {max_cost=cost;}
                	//population[j].fitness=1/(1+(alpha)*schedule_Object.TET);
                	population[j].cost=schedule_Object.TEC;
                	population[j].makespan=schedule_Object.TET;
//                	 System.out.println("	Feasibility: "+ population[j].feasibility + "	Fitness: " + population[j].fitness + "	Cost: " +population[j].cost + "	Makespan: "+ population[j].makespan);
//                     System.out.println();
                	
                	
                	
                }
                
                if (gbest.fitness < population[j].fitness) {
                    gbest = population[j]; } 
               
            }
            
            
        }

        int z=0;
        for(Task task:getTaskList()) {
        	task.setVmId(gbest.position[z]);
        	z++;
        }
        List<Cloudlet> jobs=exec(getTaskList());
        String workflow= "";
        workflow=Example_DB.workflow;
       double makespan= 0.0;
       makespan=getMKSP(jobs);
    	
    	double cost=0.0;
    	cost=getCost(getVmList(),jobs); 
        
    	double energy =0.0;
    	energy=getEnergyConsumed(jobs, getVmList());
    	double utilization = 0.0;
    	utilization = getUtil(jobs, getVmList());
    	double fitness =0;
    	fitness=gbest.fitness;
    	 try {
             FileInputStream fileInputStream = new FileInputStream("C:\\Current\\AWS\\RESULTS\\AOA.xlsx");
             Workbook workbook = WorkbookFactory.create(fileInputStream);
             fileInputStream.close();

             // Choose the sheet where you want to add data (e.g., sheet at index 0)
             Sheet sheet1 = workbook.getSheetAt(0);

             // Create a new row and cells to add your data
             Row newRow = sheet1.createRow(sheet1.getLastRowNum() + 1); // Add to the next available row

             Cell cell1 = newRow.createCell(0);
             cell1.setCellValue(workflow);
 
             Cell cell2 = newRow.createCell(1);
             cell2.setCellValue(makespan); // Replace with your second result

             Cell cell3 = newRow.createCell(2);
             cell3.setCellValue(cost); // Replace with your third result
             Cell cell4 = newRow.createCell(3);
             cell4.setCellValue(energy); // Replace with your third result
             Cell cell5 = newRow.createCell(4);
             cell5.setCellValue(utilization); // Replace with your third result
             Cell cell6 = newRow.createCell(5);
             cell6.setCellValue(fitness);
             // Create more cells and set values as needed

             // Save the updated workbook to the same file
             FileOutputStream fileOutputStream = new FileOutputStream("C:\\Current\\AWS\\RESULTS\\AOA.xlsx");
             workbook.write(fileOutputStream);
             fileOutputStream.close();

             System.out.println("Data added to the existing Excel file.");
         } catch (IOException | InvalidFormatException e) {
             e.printStackTrace();
         }

    	System.out.println("---------------------------Metrics-------------------------");
    	System.out.println("Makespan: " + makespan);
        System.out.println("Cost: " + cost);
        System.out.println("Energy Consumption: " + energy);
        System.out.println("Resource Utilization: " + utilization);
        System.out.println("---------------------------COMPLETE-------------------------");
    	}
      
    }
	private double findACC_max(double[] ACC) {
		double max = MIN;
		int i;
		for(i=0; i<dim; i++) {
			if ( ACC[i]>= max) {
				max= ACC[i];
			}}
		
		return max;
	}
	private double findACC_min(double[] ACC) {
		double min = MAX;
		int i;
		for(i=0; i<dim; i++) {
			if ( ACC[i]<=min) {
				min= ACC[i];
			}
			
		}
		return min;
	}    
}
