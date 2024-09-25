


package planning;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Calendar;
import java.util.List;

//import org.apache.poi.sl.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.core.CloudSim;
import org.workflowsim.Job;
import org.workflowsim.WorkflowDatacenter;
import org.workflowsim.WorkflowEngine;
import org.workflowsim.WorkflowPlanner;
import org.workflowsim.examples.WorkflowSimBasicExample1;
import org.workflowsim.utils.ClusteringParameters;
import org.workflowsim.utils.OverheadParameters;
import org.workflowsim.utils.Parameters;
import org.workflowsim.utils.ReplicaCatalog;
import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.Log;
import org.workflowsim.Task;
import planning.BasePlanning;
import vm.CustomVM;
import vm.CustomVMGenerator;


public class Example_DB extends WorkflowSimBasicExample1 {
	static double Deadline=0;
	static double Budget=0;
	static String workflow ="";
    /**
     * Creates main() to run this example This example has only one datacenter
     * and one storage
     */
    public static void main(String[] args) {
//    	 String outputFileName =utils.Parameters.OP_path;
//	        try {
//	            // Redirect console output to a file    
//	            PrintStream fileOut = new PrintStream(new FileOutputStream(outputFileName));
//	            System.setOut(fileOut);
    	  String daxPath= null;
     	 String directoryPath = "C:\\Current\\dax\\1\\Ordered";

 	        // Create a File object representing the directory
 	        File directory = new File(directoryPath);
  
 	        // Check if the specified path is a directory
 	        if (directory.isDirectory()) {
 	            // List the files in the directory
 	            File[] files = directory.listFiles();
 	           int file_counter=0;
 	            if (files != null) {
 	                for (File file : files) {
 	                	file_counter++;
 	                    if (file.isFile() && file.getName().endsWith(".xml")) {
 	                        // Process each input file
 	                       daxPath= file.getAbsolutePath();
 	                      workflow =file.getName();
	                    	System.out.println("_____________________________________________________"+file.getName()+"_______________________________________________");
 	                     
 	                    }
 	                    
 	                   try {
 	                      // Specify the path to your Excel file
 	                      String excelFilePath = "C:\\Current\\Normal\\RESULTS\\Deadline&Budget_HEFT.xlsx";

 	                      // Create a FileInputStream to read the Excel file
 	                      FileInputStream inputStream = new FileInputStream(new File(excelFilePath));

 	                      // Create a Workbook object (either HSSFWorkbook or XSSFWorkbook)
 	                      Workbook workbook = null;
						try {
							workbook = WorkbookFactory.create(inputStream);
						} catch (EncryptedDocumentException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (InvalidFormatException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}

 	                      // Specify the sheet name or index
 	                      Sheet sheet = workbook.getSheet("DnB4"); // Change "Sheet1" to your sheet name

 	                      // Assuming you have two columns: one for names and one for ages
 	                     int deadline = 1; // Adjust based on your Excel sheet
 	                     int budget = 6;  // Adjust based on your Excel sheet

 	                      // Iterate through rows and initialize variables with values
 	                      for (Row row : sheet) {
 	                          // Skip the header row (if any)
 	                          if (row.getRowNum() == 0) {
 	                              continue;
 	                          }
 	                         if (row.getRowNum() == file_counter) {
 	                          // Retrieve values from the specified columns
 	                         Deadline = row.getCell(deadline).getNumericCellValue();
 	                         Budget = row.getCell(budget).getNumericCellValue();

 	                          // Initialize variables with the fetched values
 	                          // You can perform further processing or store these values as needed
// 	                          System.out.println("Deadline: " + Deadline + ", Budget: " + Budget);

 	                          // Example: You might want to create objects or perform some other logic here
 	                          // Person person = new Person(name, (int) age);
 	                      }}

 	                      // Close the workbook and input stream
 	                      workbook.close();
 	                      inputStream.close();

 	                  } catch (IOException e) {
 	                      e.printStackTrace();
 	                  }
    	
       try {
            // First step: Initialize the WorkflowSim package.
 
            /**
             * However, the exact number of vms may not necessarily be vmNum If
             * the data center or the host doesn't have sufficient resources the
             * exact vmNum would be smaller than that. Take care.
             */
           int vmNum = 20;//number of vms;
           
           
            /**
             * Should change this based on real physical path
             */
       //     String daxPath = utils.Parameters.dax_path;

            File daxFile = new File(daxPath);
            if (!daxFile.exists()) {
                Log.printLine("DAX FULL PATH " + daxFile.getAbsolutePath());
                Log.printLine("Warning: Please replace daxPath with the physical path in your working environment!");
                return;
            }
            
            /**
             * Since we are using HEFT planning algorithm, the scheduling
             * algorithm should be static such that the scheduler would not
             * override the result of the planner
             */
            Parameters.SchedulingAlgorithm sch_method = Parameters.SchedulingAlgorithm.INVALID;
            //Parameters.PlanningAlgorithm pln_method = Parameters.PlanningAlgorithm.HEFT;
            ReplicaCatalog.FileSystem file_system = ReplicaCatalog.FileSystem.LOCAL;

            BasePlanning pln_method = utils.Parameters.planningAlgorithm;

            /**
             * No overheads
             */
            OverheadParameters op = new OverheadParameters(0, null, null, null, null, 0);

            /**
             * No Clustering
             */
            ClusteringParameters.ClusteringMethod method = ClusteringParameters.ClusteringMethod.NONE;
            ClusteringParameters cp = new ClusteringParameters(0, 0, method, null);

            /**
             * Initialize static parameters
             */
            Parameters.init(vmNum, daxPath, null,
                    null, op, cp, sch_method, pln_method,
                    null, 0);
            ReplicaCatalog.init(file_system);

            // before creating any entities.
            int num_user = 1;   // number of grid users
            Calendar calendar = Calendar.getInstance();
            boolean trace_flag = false;  // mean trace events

            // Initialize the CloudSim library
            CloudSim.init(num_user, calendar, trace_flag);

            WorkflowDatacenter datacenter0 = createDatacenter("Datacenter_0");

            /**
             * Create a WorkflowPlanner with one schedulers.
             */
            WorkflowPlanner wfPlanner = new WorkflowPlanner("planner_0", 1);
            /**
             * Create a WorkflowEngine.
             */
            WorkflowEngine wfEngine = wfPlanner.getWorkflowEngine();
            /**
             * Create a list of VMs.The userId of a vm is basically the id of
             * the scheduler that controls this vm.
             */
            List<CustomVM> vmlist0 = CustomVMGenerator.createCustomVMs(
                    wfEngine.getSchedulerId(0),
                    utils.Parameters.num_vms
            );

            /**
             * Submits this list of vms to this WorkflowEngine.
             */
            wfEngine.submitVmList(vmlist0, 0);

            /**
             * Binds the data centers with the scheduler.
             */
            wfEngine.bindSchedulerDatacenter(datacenter0.getId(), 0);
          //  for (int t = 0; t < utils.Parameters.simulations; t++) {
            CloudSim.startSimulation();
            List<Job> outputList0 = wfEngine.getJobsReceivedList();
            CloudSim.stopSimulation();
            printJobList(outputList0);//}
           // pln_method.printMetrics();
        } catch (Exception e) {
           // Log.printLine("The simulation has been terminated due to an unexpected error");
        }
 	               }
 	           } 
 	               }
// 	           fileOut.close();
// 	            
//	        System.out.println("Output saved to " + outputFileName);
//    } catch (Exception e) {
//	        System.err.println("Error: " + e.getMessage());
//	        }//Output file catch
   
    
    }//Main
    
} //Example

	        
	        
