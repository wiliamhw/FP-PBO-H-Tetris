package com.blockstack.views;

import javax.swing.*;

import com.blockstack.utils.Asset;
import com.blockstack.utils.Audio.Sfx;
import com.blockstack.views.menus.MainMenu;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;

public class ScoreBoard extends JPanel{

	BufferedReader scoreReader;
	TreeMap<Integer, String> scoreList;
	private final int areaWidth;
	private final int areaHeight;
	private final int wButton;
	private final int hButton;
	private JButton okay;

	private JTextField insertName = null;
	private int score = 0; 
	private int smallestScore;
	private String insertToFile;
	private int flagPlay = 0;
	private ImageIcon iconOkay = null;
	
	public ScoreBoard(int width, int height) {
		try {
			iconOkay = Asset.getImageIcon("OkayButton.png");
		} catch(Exception e) {
			System.out.println(e);
		}
		
		this.areaWidth = width;
		this.areaHeight = height;
		wButton = 110;
		hButton = 60;
		flagPlay = 0;
		setLayout(null);
		scoreList = new TreeMap<Integer, String>(Collections.reverseOrder());
		insertName = new JTextField();
		insertName.setColumns(50);
		insertName.setFocusable(true);
		score = 0;
		readFile();
		setButton();
		this.setVisible(true);
	}
	
	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		try {
			g.drawImage(Main.background, 0, 0, null);
		} catch (Exception e) {
			System.out.println(e);
		}
		int paddingTop = 60;
		g.setColor(new Color(46, 49, 49));
		g.fillRect(180, 50, 370, 475);
		
		// border
		Graphics2D g2 = (Graphics2D) g;
		Stroke oldStroke = g2.getStroke();
		g2.setStroke(new BasicStroke(10));
		g2.setColor(Color.LIGHT_GRAY);
		g2.drawRect(180, 50, 370, 475);
		g2.setStroke(oldStroke);
	
		g.setColor(Color.WHITE);
		g.setFont(new Font("Tahoma", Font.BOLD, 30));
		g.drawString("ScoreBoard", ((areaWidth/2)-87), 95);
		
		g.setFont(new Font("Tahoma", Font.PLAIN, 24));
		int yAdder = 45;
		int y = 140;
		
		Set s = scoreList.entrySet();
		Iterator i = s.iterator();
		int paddingX = 220;
		
		while(i.hasNext()) {
			Map.Entry m = (Map.Entry) i.next();
			int key = (Integer) m.getKey();
			String name = (String) m.getValue();
			g.drawString(name, paddingX, y);
			
			String strKey = Integer.toString(key);
			int x = areaWidth - g.getFontMetrics().stringWidth(strKey) - paddingX - 21; 
			g.drawString(strKey, x, y);
			y += yAdder;
		}
	}
	
	
	private void readFile() {
		FileReader scoreFileReader = null;
		
		try {
			scoreFileReader = new FileReader("src/com/blockstack/assets/score/ScoreBoard.dat");
			scoreReader = new BufferedReader(scoreFileReader);
			String line;
			while((line = scoreReader.readLine()) != null) {
				String[] tokens = line.split(":");
				scoreList.put(Integer.parseInt(tokens[1]), tokens[0]);
			}
		} catch(Exception e) { }
		
		finally {
			try {
				if(scoreFileReader != null)
					scoreFileReader.close();
			} catch (IOException e) {}
		}
	}
	
	public class ButtonHandler implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			if(e.getActionCommand().equals("Okay") || e.getActionCommand().equals("")) {
				Sfx.ok.audio.replayAudio(true);
				if(flagPlay == 1) {
					scoreList.put(score, insertName.getText());
					writeFile();
				}
				JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(okay.getParent());
				frame.setContentPane(new MainMenu(areaWidth, areaHeight));
				frame.invalidate();
				frame.validate();
			}
		}
	}
	
	public void setScore(int score) {
		flagPlay = 1;
		if(scoreList != null)
			smallestScore = scoreList.lastKey();
		if(score > smallestScore) {
			this.score = score;
			scoreList.put(score, "");
			scoreList.pollLastEntry();
			
			int index = 0;
			for(Map.Entry mapElement : scoreList.entrySet()) {
				int key = (int)mapElement.getKey();
				if(key == score)
					break;
				index++;
			}
			insertName.setFont(new Font("Tahoma", Font.PLAIN, 24));
			insertName.setBounds(220, (140+index*45)-30, 120, 35);
			this.add(insertName);
		}
	}
	
	private void writeFile() {
		insertToFile = "";
		int index = 0;
		for(Map.Entry mapElement : scoreList.entrySet()) {
			int key = (int)mapElement.getKey();
			String value = (String)mapElement.getValue();
			String temp = "";
			if (value.length() >= 1) {
				temp = value.substring(0, 1).toUpperCase();
				if (value.length() != 1) {
					temp += value.substring(1, Math.min(value.length(), 8));
				}
			}
			value = temp;
			if(index < 8) {
				insertToFile = insertToFile + value + ":" + Integer.toString(key)+"\n";
			} else {
				insertToFile = insertToFile + value + ":" + Integer.toString(key);
			}
			index++;
		}
		
		File scoreFile = Asset.getFile("score", "ScoreBoard.dat");
		Writer writeFile = null;
		try {
			if(!scoreFile.createNewFile()) {
				scoreFile.delete();
				scoreFile.createNewFile();
				writeFile = new FileWriter(scoreFile);
				
				writeFile.write(insertToFile);
			}
		} catch (Exception e) {}
		
		finally {
			try {
				if(writeFile != null)
					writeFile.close();
			} catch (IOException e) {}
		}
	}
	
	private void setHover(JButton button) {
		button.addMouseListener(new MouseAdapter() {
			public void mouseEntered(MouseEvent e) {
				Sfx.cursor.audio.replayAudio(true);
				button.setBorder(BorderFactory.createLineBorder(Color.GREEN, 5));
			}
			public void mouseExited(MouseEvent e) {
				button.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 5));
			}
		});
	}
	
	private void setButton() {
		ButtonHandler handler = new ButtonHandler();
		
		if(iconOkay == null) {
			okay = new JButton("Okay");			
		}
		else {
			okay = new JButton("", iconOkay);
		}
		
		okay.setHorizontalTextPosition(JButton.CENTER);
		okay.setBounds(((areaWidth/2)-(wButton/2)), 535, wButton, hButton);
		okay.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 5));
		okay.addActionListener(handler);
		setHover(okay);
		this.add(okay);
	}
}
