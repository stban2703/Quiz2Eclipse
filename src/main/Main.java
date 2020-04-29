package main;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.time.temporal.JulianFields;
import java.util.ArrayList;

import com.google.gson.Gson;

import processing.core.PApplet;

public class Main extends PApplet {

	public static void main(String[] args) {
		PApplet.main("main.Main");

	}

	private DatagramSocket socket;
	private InetAddress android;
	private ArrayList<Punto> puntos;

	public void settings() {
		size(1080 / 2, 1884 / 2);
	}

	public void setup() {
		puntos = new ArrayList<Punto>();

		// 1. Hilo de recepcion
		new Thread(() -> {
			try {
				android = InetAddress.getByName("192.168.0.4");
				socket = new DatagramSocket(5000);

				while (true) {
					// Si es un Datagram de recepcion, solo le ponemos dos parametros
					byte[] buffer = new byte[2000]; // -> Entero que puede representar 256, ASCII, 8 bit, bit -> -0 o 1
					DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

					// Esta linea, se queda esperando por paquetes
					socket.receive(packet);

					// El mensaje queda en el paquete, despues de la recepcion
					String json = new String(packet.getData()).trim();

					// Limpiar arreglo
					puntos.clear();

					// Llenar arreglo con lo que recibo
					Gson gson = new Gson();
					Punto[] puntosRecibidos = gson.fromJson(json, Punto[].class);
					for (int i = 0; i < puntosRecibidos.length; i++) {
						// Ajustar posicion
						float mitadX = puntosRecibidos[i].getPosX() / 2;
						float mitadY = puntosRecibidos[i].getPosY() / 2;
						
						puntosRecibidos[i].setPosX(mitadX);
						puntosRecibidos[i].setPosY(mitadY);
						
						puntos.add(puntosRecibidos[i]);
					}

				}

			} catch (SocketException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}).start();

	}

	public void draw() {
		background(255);

		// Pintar jugada
		for (int i = 0; i < puntos.size(); i++) {
			float x = puntos.get(i).getPosX();
			float y = puntos.get(i).getPosY();
			int tipo = puntos.get(i).getTipo();

			noStroke();
			if (tipo == 0) {
				fill(255, 155, 0);
				ellipse(x, y, 50, 50);
			} else if (tipo == 1) {
				fill(155, 0, 255);
				ellipse(x, y, 50, 50);
			}

		}

	}

	public void mousePressed() {
		// Enviar jugada

		Punto punto = new Punto(mouseX, mouseY, 0);
		puntos.add(punto);
		Gson gson = new Gson();
		String json = gson.toJson(puntos);
		enviarMensaje(json);

	}

	public void enviarMensaje(String mensaje) {
		// 2. Hilo de envio
		new Thread(() -> {
			try {
				byte[] buffer = mensaje.getBytes();
				// El paquete tiene 4 parametros
				DatagramPacket packet = new DatagramPacket(buffer, buffer.length, android, 5000);
				socket.send(packet);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}).start();
	}

}
