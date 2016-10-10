package main;

import java.util.ArrayList;

public class QCMIN {
	int num_inputs;
	ArrayList<int[]> implicants;
	public QCMIN(int n, int[] values){
		//Accepts two arguments: the number of input variables and a list of values
		num_inputs = n;
		implicants = new ArrayList<int[]>();
		for(int i=0;i<(int)Math.pow(2, num_inputs);i++){
			if(values[i] == 1){//If this combination of inputs is strictly a 1
				String bin_rep = ("000"+Integer.toBinaryString(i)).substring(Integer.toBinaryString(i).length());
				int[] temp = new int[num_inputs];
				for(int j=0;j<temp.length;j++){
					temp[j] = bin_rep.charAt(j) == 48 ? 0 : 1;
				}
				implicants.add(temp);
			}
		}//Added all terms with 1 into list of implicants
		boolean changed = true;
		while(changed){
			//System.out.println("New Iteration commenced");
			changed = false;
			/*System.out.println("");
			System.out.println("Commencing print");
			for(int[] term : implicants){
				System.out.println(term[0]+", "+term[1]+", "+term[2]);
			}*/
			ArrayList<int[]> next_implicants = new ArrayList<int[]>();
			for(int[] i: implicants){
				boolean matched = false;
				for(int[] j:implicants){
					int mismatch = 0;
					int num_mismatch = 0;
					for(int k=0;k<num_inputs;k++){
						if(i[k]!=j[k]){mismatch=k;num_mismatch++;}
					}
					if(num_mismatch==1){
						int[] temp = new int[]{j[0],j[1],j[2]};
						temp[mismatch] = -1;
						next_implicants.add(temp);
						matched = true;
						changed = true;
					}
				}
				if(!matched) next_implicants.add(i);
			}
			implicants = next_implicants;
			/*System.out.println("");
			System.out.println("Before reduction");
			for(int[] term : implicants){
				System.out.println(term[0]+", "+term[1]+", "+term[2]);
			}*/
			ArrayList<int[]> reduced_implicants = new ArrayList<int[]>();
			for(int i=0;i<implicants.size();i++){
				boolean matchfound = false;
				int[] i_e = implicants.get(i);
				for(int j=i+1;j<implicants.size();j++){
					int[] j_e = implicants.get(j);
					int num_mismatch = 0;
					for(int k=0;k<num_inputs;k++){
						if(i_e[k]!=j_e[k]){num_mismatch++;}
					}
					if(num_mismatch==0){matchfound = true;}
				}
				if(!matchfound){reduced_implicants.add(i_e);}
			}
			implicants = reduced_implicants;
			if(!changed){break;}
		}
	}
	public void printmin(){
		for(int[] term : implicants){
			System.out.println(term[0]+", "+term[1]+", "+term[2]);
		}
	}
}