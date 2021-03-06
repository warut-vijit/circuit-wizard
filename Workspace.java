package main;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Hashtable;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;

public class Workspace extends JPanel {
	private ArrayList<Gate> detached_gates;
	private Gate selected;
	private boolean addWireState; //t/f about whether wires being added
	private boolean[] keyStates; //boolean state of keys by keycode
	private Hashtable<String,Object> backlog;
	private ArrayList<bool_in> ext_inputs;
	private ArrayList<bool_out> ext_outputs;
	private Thread liveUpdate;
	public static void main(String[] args){
		Workspace w = new Workspace();
		w.setFocusable(true);
		w.requestFocus();
		w.addMouseListener(new MouseListener(){//Mouse listener
			public void mouseClicked(MouseEvent arg0) {w.update(w.getGraphics());}
			public void mouseEntered(MouseEvent arg0) {}
			public void mouseExited(MouseEvent arg0) {}
			public void mousePressed(MouseEvent arg0) {w.mouse_down_listener(arg0);w.update(w.getGraphics());}
			public void mouseReleased(MouseEvent arg0) {w.mouse_up_listener(arg0);w.update(w.getGraphics());}
		});
		w.addKeyListener(new KeyListener() {//Keyboard shortcuts
			public void keyTyped(KeyEvent e) {}
			public void keyReleased(KeyEvent e) {w.processPress(e.getKeyCode(),false);}
			public void keyPressed(KeyEvent e) {w.processPress(e.getKeyCode(),true);}
		});
		w.liveUpdate = new Thread(){
			public void run(){
				while(true){
					try {
						w.repaint();
						Thread.sleep(50);
					}catch(NullPointerException npe) {}
					catch(InterruptedException ie){}
				}
			}
		};
		w.liveUpdate.start();
		JFrame jf = new JFrame();
		JMenuBar menubar = new JMenuBar();
			JMenu file = new JMenu("File");
			menubar.add(file);
			JMenu edit = new JMenu("Edit");
			menubar.add(edit);
			JMenu verify = new JMenu("Verify");
				JMenuItem verify_all = new JMenuItem("Full Circuit [Ctrl+F]");
				verify_all.addActionListener(new ActionListener(){public void actionPerformed(ActionEvent e){w.displayOutput();}});
				verify.add(verify_all);
				JMenuItem check_min = new JMenuItem("Check Minimal");
				check_min.addActionListener(new ActionListener(){public void actionPerformed(ActionEvent e){w.checkMinimal();}});
				verify.add(check_min);
			menubar.add(verify);
			JMenu add = new JMenu("Gates");
				JMenuItem AND = new JMenuItem("AND");
				AND.addActionListener(new ActionListener(){public void actionPerformed(ActionEvent e){w.add_gate("AND");}});
				add.add(AND);
				JMenuItem OR = new JMenuItem("OR");
				OR.addActionListener(new ActionListener(){public void actionPerformed(ActionEvent e){w.add_gate("OR");}});
				add.add(OR);
				JMenuItem NOT = new JMenuItem("NOT");
				NOT.addActionListener(new ActionListener(){public void actionPerformed(ActionEvent e){w.add_gate("NOT");}});
				add.add(NOT);
				JMenuItem XOR = new JMenuItem("XOR");
				XOR.addActionListener(new ActionListener(){public void actionPerformed(ActionEvent e){w.add_gate("XOR");}});
				add.add(XOR);
				JMenuItem NAND = new JMenuItem("NAND");
				NAND.addActionListener(new ActionListener(){public void actionPerformed(ActionEvent e){w.add_gate("NAND");}});
				add.add(NAND);
				JMenuItem NOR = new JMenuItem("NOR");
				NOR.addActionListener(new ActionListener(){public void actionPerformed(ActionEvent e){w.add_gate("NOR");}});
				add.add(NOR);
				JMenuItem NXOR = new JMenuItem("NXOR");
				NXOR.addActionListener(new ActionListener(){public void actionPerformed(ActionEvent e){w.add_gate("NXOR");}});
				add.add(NXOR);
				JMenuItem delin = new JMenuItem("--------");
				add.add(delin);
				JMenuItem INPUT = new JMenuItem("INPUT");
				INPUT.addActionListener(new ActionListener(){public void actionPerformed(ActionEvent e){w.add_gate("INPUT");}});
				add.add(INPUT);
				JMenuItem OUTPUT = new JMenuItem("OUTPUT");
				OUTPUT.addActionListener(new ActionListener(){public void actionPerformed(ActionEvent e){w.add_gate("OUTPUT");}});
				add.add(OUTPUT);
			menubar.add(add);
			JMenu othercomp = new JMenu("Other Elements");
				JMenuItem SIGNAL = new JMenuItem("Signal [Ctrl+S]");
				SIGNAL.addActionListener(new ActionListener(){public void actionPerformed(ActionEvent e){w.addWireState=true;w.update(w.getGraphics());}});
				othercomp.add(SIGNAL);
			menubar.add(othercomp);
		jf.setJMenuBar(menubar);
		JPanel jp = new JPanel();
		jp.setLayout(new GridLayout(1,1));
		jp.add(w);
		jf.add(jp);
		jp.setSize(1000,700);
		jf.setSize(1000,700);
		jf.setResizable(false);
	    jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		jf.setVisible(true);
	}
	public Workspace(){
		super();
		Workspace w = this;
		addWireState = false;
		detached_gates = new ArrayList<Gate>();
		selected = null;
		keyStates = new boolean[100];
		backlog = new Hashtable<String,Object>();
		ext_inputs = new ArrayList<bool_in>();
		ext_outputs = new ArrayList<bool_out>();
	}
	public void paint(Graphics g){
		int block_size = 15;
		g.setColor(Color.WHITE);
		g.fillRect(0, 0, this.getWidth(), this.getHeight());
		g.setColor(Color.BLACK);
		for(int i=0;i<this.getWidth();i+=block_size){
			for(int j=0;j<this.getHeight();j+=block_size){
				g.fillRect(i, j, 1, 1);
			}
		}
		
		for(Gate gate : detached_gates){
			switch(gate.type){
				case "INPUT":
					g.setColor(Color.BLACK);
					int[] x = {gate.x-15,gate.x,gate.x+10,gate.x,gate.x-15};
					int[] y = {gate.y-15,gate.y-15,gate.y,gate.y+15,gate.y+15};
					g.drawPolygon(x,y,5);
					g.drawLine(gate.x+10, gate.y, gate.x+15, gate.y);
					g.drawString(gate.name, gate.x-7, gate.y+7);
					break;
				case "OUTPUT":
					g.setColor(Color.BLACK);
					int[] x2 = {gate.x+15,gate.x,gate.x-10,gate.x,gate.x+15};
					int[] y2 = {gate.y+15,gate.y+15,gate.y-7,gate.y-15,gate.y-15};
					g.drawPolygon(x2,y2,5);
					g.drawLine(gate.x-10, gate.y, gate.x-15, gate.y);
					g.drawString(gate.name, gate.x, gate.y);
					break;
				case "NOT":
					g.setColor(Color.BLACK);
					int[] x_coords6 = {gate.x-5,gate.x+10,gate.x-5};
					int[] y_coords6 = {gate.y-15,gate.y,gate.y+15};
					g.fillPolygon(x_coords6,y_coords6,3);
					g.setColor(Color.BLACK);
					g.drawLine(gate.x-15, gate.y-7, gate.x-5, gate.y-7);
					g.drawLine(gate.x+10, gate.y, gate.x+15, gate.y);
					break;
				case "AND":
					g.setColor(Color.BLUE);
					g.fillArc(gate.x-20, gate.y-15, 30, 30, -90, 180);
					g.setColor(Color.BLACK);
					g.drawLine(gate.x-15, gate.y-7, gate.x-5, gate.y-7);
					g.drawLine(gate.x-15, gate.y+7, gate.x-5, gate.y+7);
					g.drawLine(gate.x+10, gate.y, gate.x+15, gate.y);
					break;
				case "NAND":
					g.setColor(new Color(0,0,127));
					g.fillArc(gate.x-20, gate.y-15, 30, 30, -90, 180);
					g.setColor(Color.BLACK);
					g.drawLine(gate.x-15, gate.y-7, gate.x-5, gate.y-7);
					g.drawLine(gate.x-15, gate.y+7, gate.x-5, gate.y+7);
					g.drawLine(gate.x+10, gate.y, gate.x+15, gate.y);
					break;
				case "OR":
					g.setColor(Color.RED);
					int[] x_coords = {gate.x-5,gate.x+10,gate.x-5,gate.x};
					int[] y_coords = {gate.y-15,gate.y,gate.y+15,gate.y};
					g.fillPolygon(x_coords,y_coords,4);
					g.setColor(Color.BLACK);
					g.drawLine(gate.x-15, gate.y-7, gate.x-5, gate.y-7);
					g.drawLine(gate.x-15, gate.y+7, gate.x-5, gate.y+7);
					g.drawLine(gate.x+10, gate.y, gate.x+15, gate.y);
					break;
				case "NOR":
					g.setColor(new Color(127,0,0));
					int[] x_coords3 = {gate.x-5,gate.x+10,gate.x-5,gate.x};
					int[] y_coords3 = {gate.y-15,gate.y,gate.y+15,gate.y};
					g.fillPolygon(x_coords3,y_coords3,4);
					g.setColor(Color.BLACK);
					g.drawLine(gate.x-15, gate.y-7, gate.x-5, gate.y-7);
					g.drawLine(gate.x-15, gate.y+7, gate.x-5, gate.y+7);
					g.drawLine(gate.x+10, gate.y, gate.x+15, gate.y);
					break;
				case "XOR":
					g.setColor(Color.GREEN);
					int[] x_coords2 = {gate.x-5,gate.x+10,gate.x-5,gate.x};
					int[] y_coords2 = {gate.y-15,gate.y,gate.y+15,gate.y};
					g.fillPolygon(x_coords2,y_coords2,4);
					g.drawLine(gate.x-9,gate.y-15,gate.x-4,gate.y);
					g.drawLine(gate.x-9,gate.y+15,gate.x-4,gate.y);
					g.setColor(Color.BLACK);
					g.drawLine(gate.x-15, gate.y-7, gate.x-5, gate.y-7);
					g.drawLine(gate.x-15, gate.y+7, gate.x-5, gate.y+7);
					g.drawLine(gate.x+10, gate.y, gate.x+15, gate.y);
					break;
				case "NXOR":
					g.setColor(new Color(0,127,0));
					int[] x_coords4 = {gate.x-5,gate.x+10,gate.x-5,gate.x};
					int[] y_coords4 = {gate.y-15,gate.y,gate.y+15,gate.y};
					g.fillPolygon(x_coords4,y_coords4,4);
					g.drawLine(gate.x-9,gate.y-15,gate.x-4,gate.y);
					g.drawLine(gate.x-9,gate.y+15,gate.x-4,gate.y);
					g.setColor(Color.BLACK);
					g.drawLine(gate.x-15, gate.y-7, gate.x-5, gate.y-7);
					g.drawLine(gate.x-15, gate.y+7, gate.x-5, gate.y+7);
					g.drawLine(gate.x+10, gate.y, gate.x+15, gate.y);
					break;
				default:	
					g.fillRect(gate.x-15,gate.y-15,30,30);
					break;
			}
			for(Gate child : gate.children){
				int position = child.parents.indexOf(gate)==0 ? -7 : 7;
				g.drawLine(gate.x+15, gate.y, gate.x+15, child.y+position);
				g.drawLine(gate.x+15, child.y+position, child.x-15, child.y+position);
			}
			if(selected!=null && addWireState==true){
				Point pos = MouseInfo.getPointerInfo().getLocation();
				g.drawLine(selected.x+15, selected.y, selected.x+15, (int)(pos.getY()-this.getLocationOnScreen().getY()));
				g.drawLine(selected.x+15, (int)(pos.getY()-this.getLocationOnScreen().getY()), (int)(pos.getX()-this.getLocationOnScreen().getX()), (int)(pos.getY()-this.getLocationOnScreen().getY()));
			}
		}
	}
	public void add_gate(String id){
		String name = "";
		if(id.equals("INPUT") || id.equals("OUTPUT")){
			name = JOptionPane.showInputDialog(this, "Port name: ");
		}
		switch(id){
			case "AND": detached_gates.add(new ANDgate(60,60));break;
			case "OR": detached_gates.add(new ORgate(60,60));break;
			case "NOT": detached_gates.add(new NOTgate(60,60));break;
			case "XOR": detached_gates.add(new XORgate(60,60));break;
			case "NAND": detached_gates.add(new NANDgate(60,60));break;
			case "NOR": detached_gates.add(new NORgate(60,60));break;
			case "NXOR": detached_gates.add(new NXORgate(60,60));break;
			case "INPUT": bool_in b = new bool_in(name,60,60);detached_gates.add(b);ext_inputs.add(b);break;
			case "OUTPUT": bool_out bout = new bool_out(name,60,60);detached_gates.add(bout);ext_outputs.add(bout);break;
		}
		this.update(this.getGraphics());
	}
	private void mouse_down_listener(MouseEvent m){
		for(int i=0;i<detached_gates.size();i++){
			Gate gate = detached_gates.get(detached_gates.size()-1-i);//Layering behavior
			if(Math.abs(gate.x-m.getX())+Math.abs(gate.y-m.getY())<30){
				selected = gate;//Gate to be moved in non wire add, gate for wire origin in wire add
				break;
			}
		}
	}
	protected void updateWorkspace() {
		this.update(this.getGraphics());
	}
	private void mouse_up_listener(MouseEvent m){
		if(!addWireState){
			if(selected!=null){
				selected.x = m.getX()/15*15;
				selected.y = m.getY()/15*15;
				selected=null;
			}
		}else{
			for(Gate gate : detached_gates){
				if(Math.abs(gate.x-m.getX())+Math.abs(gate.y-m.getY())<30 && selected!=null){//If a gate has been selected
					selected.addChild(gate);
					break;
				}
			}
			selected = null;
			addWireState = false;
		}
	}
	private void processPress(int code, boolean state){//Event handler for key press
		keyStates[code] = state; //Update key state list
		//System.out.println(code);
		if(keyStates[90] && keyStates[17]) System.out.println("Undo"); //Control+Z
		else if(keyStates[83] && keyStates[17]){keyStates[83]=false;keyStates[17]=false;addWireState = true;}
		else if(keyStates[70] && keyStates[17]){keyStates[70]=false;keyStates[17]=false;displayOutput();} 
	}
	private Boolean[][] verifyFullCircuit(){
		int numinputs = ext_inputs.size();
		int numoutputs = ext_outputs.size();
		Boolean[][] tt = new Boolean[(int)Math.pow(2, numinputs)][numinputs+numoutputs];
		for(int val=0;val<(int)Math.pow(2,numinputs);val++){
			for(int i=0;i<numinputs;i++){
				ext_inputs.get(i).state = (val>>i)%2==1 ? true : false;
				tt[val][i] = ext_inputs.get(i).state;
			}
			for(bool_in i : ext_inputs){
				i.update();
			}
			for(int j=0;j<numoutputs;j++){
				bool_out i = ext_outputs.get(j);
				tt[val][j+numinputs] = i.state;
			}
		}
		
		return tt;
	}
	private void displayOutput(){
		int numinputs = ext_inputs.size();
		int numoutputs = ext_outputs.size();
		String[] titles = new String[numinputs+numoutputs];
		for(int i = 0;i<numinputs;i++){
			titles[i] = ext_inputs.get(i).name;
		}
		for(int i = 0;i<numoutputs;i++){
			titles[i+numinputs] = ext_outputs.get(i).name;
		}
		Boolean[][] tt = verifyFullCircuit();
		JDialog jd = new JDialog();//Circuit output
		JTable jt = new JTable(tt,titles);
		JScrollPane jsp = new JScrollPane(jt);
		jsp.addMouseListener(new MouseListener(){
			public void mouseClicked(MouseEvent arg0) {}
			public void mouseEntered(MouseEvent e) {}
			public void mouseExited(MouseEvent e) {}
			public void mousePressed(MouseEvent e) {}
			public void mouseReleased(MouseEvent e) {}
		});
		jd.add(jsp);
		jd.pack();
		jd.setVisible(true);
	}
	private void checkMinimal() {
		Boolean[][] values = verifyFullCircuit();
		for(int j=0;j<ext_outputs.size();j++){
			int[] ogvalues = new int[values.length];
			for(int i=0;i<values.length;i++){
				ogvalues[i]=(values[i][ext_inputs.size()+j]) ? 1 : 0;
			}
			QCMIN q = new QCMIN(ext_inputs.size(),ogvalues);
			ArrayList<int[]> minimized = q.printmin();
			String iostring = "";
			for(int[] i : minimized){
				for(int input=0;input<i.length;input++){
					if(i[input]==0){iostring+=ext_inputs.get(ext_inputs.size()-1-input).name()+"'";}
					else if(i[input]==1){iostring+=ext_inputs.get(ext_inputs.size()-1-input).name();}
				}
				iostring += " + ";
			}
			JDialog jd = new JDialog();//Minimized Expression
			jd.add(new JLabel(iostring.substring(0, iostring.length()-2)));
			jd.pack();
			jd.setVisible(true);
		}
	}
}






class ANDgate extends Gate{
	public ANDgate(int x, int y){
		super(x, y);
		type = "AND";
	}
	public void update(){
		switch(parents.size()){
			case 0:
				state = false;
				break;
			case 1:
				state = parents.get(0).getState();
				break;
			default:
				boolean b = true;
				for(Gate g : this.parents){
					b = (b && g.getState());
				}
				state = b;
				break;
		}
		for(Gate g : children){
			g.update();
		}
	}
}
class bool_in extends Gate{
	public bool_in(String s, int x, int y){
		super(x, y);
		name = s;
		type = "INPUT";
	}
	public void update(){
		for (Gate g: children) g.update();
	}
	public String name(){
		return name;
	}
}
class bool_out extends Gate{
	public bool_out(String s, int a, int b) {
		super(a, b);
		name = s;
		type = "OUTPUT";
	}
	public void update(){
		switch(parents.size()){
			case 0:
				state = false;
				break;
			default:
				state = parents.get(0).getState();
				break;
		}
	}
}
class NANDgate extends Gate{
	public NANDgate(int x, int y){
		super(x, y);
		type="NAND";
	}
	public void update(){
		switch(parents.size()){
			case 0:
				state = false;
				break;
			case 1:
				state = !parents.get(0).getState();
				break;
			default:
				boolean b = true;
				for(Gate g : this.parents){
					b = (b && g.getState());
				}
				state = !b;
				break;
		}
		for(Gate g : children){
			g.update();
		}
	}
}
class NORgate extends Gate{
	public NORgate(int x, int y){
		super(x, y);
		type="NOR";
	}
	public void update(){
		switch(parents.size()){
			case 0:
				state = false;
				break;
			case 1:
				state = !parents.get(0).getState();
				break;
			default:
				boolean b = false;
				for(Gate g : this.parents){
					b = (b || g.getState());
				}
				state = !b;
				break;
		}
		for(Gate g : children){
			g.update();
		}
	}
}
class NOTgate extends Gate{
	public NOTgate(int x, int y){
		super(x, y);
		type="NOT";
	}
	public void update(){
		switch(parents.size()){
			case 0:
				state = false;
				break;
			case 1:
				state = !parents.get(0).getState();
				break;
			default:
				state = false;
				break;
		}
		for(Gate g : children){
			g.update();
		}
	}
}
class NXORgate extends Gate{
	public NXORgate(int x, int y){
		super(x, y);
		type="NXOR";
	}
	public void update(){
		switch(parents.size()){
			case 0:
				state = false;
				break;
			case 1:
				state = true;
				break;
			default:
				boolean b = false;
				for(Gate g : this.parents){
					b = (b ^ g.getState());
				}
				state = !b;
				break;
		}
		for(Gate g : children){
			g.update();
		}
	}
}
class ORgate extends Gate{
	public ORgate(int x, int y){
		super(x, y);
		type="OR";
	}
	public void update(){
		switch(parents.size()){
			case 0:
				state = false;
				break;
			case 1:
				state = parents.get(0).getState();
				break;
			default:
				boolean b = false;
				for(Gate g : this.parents){
					b = (b || g.getState());
				}
				state = b;
				break;
		}
		for(Gate g : children){
			g.update();
		}
	}
}
class XORgate extends Gate{
	public XORgate(int x, int y){
		super(x, y);
		type="XOR";
	}
	public void update(){
		switch(parents.size()){
			case 0:
				state = false;
				break;
			case 1:
				state = false;
				break;
			default:
				boolean b = false;
				for(Gate g : this.parents){
					b = (b ^ g.getState());
				}
				state = b;
				break;
		}
		for(Gate g : children){
			g.update();
		}
	}
}
class Gate {
	ArrayList<Gate> children;
	ArrayList<Gate> parents;
	String type;
	String name;
	int x, y;
	boolean state;
	public Gate(int a, int b){
		children = new ArrayList<Gate>();
		parents = new ArrayList<Gate>();
		x = a;
		y = b;
		state = false;
	}
	public void update(){}
	public void addChild(Gate g){
		children.add(g);
		g.addParent(this);
	}
	public void addParent(Gate g){//Will be executed as subroutine of addChild for parent gate
		parents.add(g);
	}
	public void moveTo(int a, int b){
		x = a;
		y = b;
	}
	public boolean getState(){
		return state;
	}
}
