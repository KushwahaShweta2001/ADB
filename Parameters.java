package utils;

import planning.AOA_DB;
import planning.BasePlanning;


public interface Parameters {

	
    int num_vms = 20;
    int simulations = 50;


  BasePlanning planningAlgorithm = new AOA_DB();

  
  
}
