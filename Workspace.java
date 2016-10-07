package main;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

public class Workspace extends JPanel {
	private ArrayList<Gate> detached_gates;
	private Gate selected;
	private boolean addWireState;
	public static void main(String[] args){
		Workspace w = new Workspace();
		w.addMouseListener(new MouseListener(){

			@Override
			public void mouseClicked(MouseEvent arg0) {w.update(w.getGraphics());}
			public void mouseEntered(MouseEvent arg0) {}
			public void mouseExited(MouseEvent arg0) {}
			public void mousePressed(MouseEvent arg0) {w.mouse_down_listener(arg0);w.update(w.getGraphics());}
			public void mouseReleased(MouseEvent arg0) {w.mouse_up_listener(arg0);w.update(w.getGraphics());}
		});
		JFrame jf = new JFrame();
		JMenuBar menubar = new JMenuBar();
			JMenu file = new JMenu("File");
			menubar.add(file);
			JMenu edit = new JMenu("Edit");
			menubar.add(edit);
			JMenu verify = new JMenu("Verify");
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
				JMenuItem SIGNAL = new JMenuItem("Signal");
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
		addWireState = false;
		detached_gates = new ArrayList<Gate>();
		selected = null;
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
					int[] y2 = {gate.y+15,gate.y+15,gate.y,gate.y-15,gate.y-15};
					g.drawPolygon(x2,y2,5);
					g.drawLine(gate.x-10, gate.y, gate.x-15, gate.y);
					g.drawString(gate.name, gate.x, gate.y);
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
				g.drawLine(gate.x+15, gate.y, child.x-15, child.y+position);
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
			case "INPUT": detached_gates.add(new bool_in(name,60,60));break;
			case "OUTPUT": detached_gates.add(new bool_out(name,60,60));break;
		}
		this.update(this.getGraphics());
	}
	public void mouse_down_listener(MouseEvent m){
		for(Gate gate : detached_gates){
			if(Math.abs(gate.x-m.getX())+Math.abs(gate.y-m.getY())<30){
				selected = gate;//Gate to be moved in non wire add, gate for wire origin in wire add
				break;
			}
		}
	}
	public void mouse_up_listener(MouseEvent m){
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