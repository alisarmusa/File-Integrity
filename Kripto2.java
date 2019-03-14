import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

public class Kripto2 {

	public static File[] listTest(File dir) {
		File[] list = dir.listFiles();

		return list;
	}

	public static void main(String[] args) throws IOException {

		BigInteger p = new BigInteger("1301");
		BigInteger q = new BigInteger("1453");
		BigInteger n = p.multiply(q);

		Scanner scanner1 = new Scanner(System.in);

		String command = scanner1.nextLine();
		String[] word = command.split(" ");

		String folderPath = "";
		String registryPath = "";
		String logFile = "";
		String hashFunction = "";
		String privateKey = "";
		String publicKey = "";
		String intervalTime = "";

		String[] cmd = { "sh", "Myfile.sh", folderPath };

		if (word.length == 2) {
			System.exit(0);
		} else {
			// Arguman Sirasi Değisimi
			for (int i = 0; i < word.length; i++) {
				if (word[i].equals("-p")) {
					folderPath = word[i + 1];
				}
				if (word[i].equals("-r")) {
					registryPath = word[i + 1];
				}
				if (word[i].equals("-l")) {
					logFile = word[i + 1];
				}
				if (word[i].equals("-h")) {
					hashFunction = word[i + 1];
				}
				if (word[i].equals("-k")) {
					privateKey = word[i + 1];
					publicKey = word[i + 2];
				}
				if (word[i].equals("-i")) {
					intervalTime = word[i + 1];
				}
			}

			String bashcommand = "Kripto2 start -p " + folderPath + " -r " + registryPath + " -l " + logFile + " -h "
					+ hashFunction + " -k " + privateKey + publicKey + " -i " + intervalTime;
/*
			try {
				FileWriter fw = new FileWriter("script.sh");

				PrintWriter pw = new PrintWriter(fw);

				pw.println("#!/bin/bash");
				pw.println(bashcommand);
				pw.println("sleep " + intervalTime + "m");
				pw.println("done");
				pw.close();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

			Process proc = null;

			try {
				//proc = Runtime.getRuntime().exec("script.sh");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
*/
			String pub = "";
			String pri = "";

			try (BufferedReader br = new BufferedReader(new FileReader(publicKey))) {
				String line;
				while ((line = br.readLine()) != null) {
					pub += line;
				}
			}

			try (BufferedReader br = new BufferedReader(new FileReader(privateKey))) {
				String line;
				while ((line = br.readLine()) != null) {
					pri += line;
				}
			}

			byte[] pubKey = pub.getBytes();
			byte[] priKey = pri.getBytes();

			File dir = new File(folderPath);
			File[] mylist = listTest(dir);

			ArrayList<String> hashList = new ArrayList<String>();
			String md5hash = "";

			for (int i = 0; i < mylist.length; i++) {
				FileInputStream fis = null;
				try {
					File file = new File("" + mylist[i]);
					if (!file.exists() || file.length() == 0) {
						throw new RuntimeException("Bad input :-(");
					}

					fis = new FileInputStream(file);
					byte[] fileBytes = new byte[(int) file.length()];
					MessageDigest md = MessageDigest.getInstance(hashFunction);
					int length;
					while ((length = fis.read(fileBytes)) != -1) {
						md.update(fileBytes, 0, length);
					}
					byte[] raw = md.digest();
					StringBuilder sb = new StringBuilder();
					for (int j = 0; j < raw.length; j++) {
						byteToHex(raw[j], sb);
					}
					hashList.add(mylist[i].toString());
					hashList.add(sb.toString());
					md5hash += sb.toString();

				} catch (NoSuchAlgorithmException | IOException e) {
					e.printStackTrace();
				} finally {
					if (fis != null) {
						try {
							fis.close();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}
			}

			byte[] bytes = md5hash.getBytes();
			BigInteger plainNum = new BigInteger(bytes);

			BigInteger pubKey2 = new BigInteger(pubKey);
			BigInteger priKey2 = new BigInteger(priKey);

			BigInteger enc = plainNum.modPow(pubKey2, n);

			String reg = "";
			String[] myreg = null;

			File registry = new File(registryPath);
			String[] mystring = readRegistry(reg, myreg, registry);

			if (registry.exists()) {
				createLog(logFile, mystring, hashList);
			}

			for (int i = 0; i < word.length; i++) {
				if (word[i].equals("start")) {
					File myfile = new File(registryPath);
					createRegistry(registryPath, hashList, enc, myfile);
				}
			}

		} // else

	} // main

	private static StringBuilder byteToHex(byte b, StringBuilder sb) {
		String hexVal = Integer.toHexString((b & 0xff));
		if (hexVal.length() == 1) {
			return sb.append("0").append(hexVal);
		}
		return sb.append(hexVal);
	}

	public static void createLog(String logFile, String[] newmyreg, ArrayList hashList) throws IOException {

		DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyy HH:mm:ss");
		Calendar cal = Calendar.getInstance();

		File file = new File(logFile);
		FileWriter writer = new FileWriter(file, true);
		BufferedWriter w = new BufferedWriter(writer);

		// Altered
		int verisayac = 0;
		for (int i = 0; i < hashList.size(); i += 2) {
			for (int j = 0; j < newmyreg.length; j += 2) {
				if (newmyreg[j].equals(hashList.get(i))) {
					if (newmyreg[j + 1].equals(hashList.get(i + 1))) {
						verisayac++;
					} else {
						w.write(dateFormat.format(cal.getTime()) + " : " + newmyreg[i] + " Altered\n");
					}
				}
			}
		}
		if (verisayac == newmyreg.length / 2) {
			w.write(dateFormat.format(cal.getTime()) + " : " + "Verification Failed\n");
		}

		// Delete
		for (int i = 0; i < newmyreg.length; i += 2) {
			int sayac = 0;
			for (int j = 0; j < hashList.size(); j += 2) {
				if (newmyreg[i].equals(hashList.get(j))) {

				} else {
					sayac++;
				}
			}
			if (sayac == hashList.size() / 2) {
				w.write(dateFormat.format(cal.getTime()) + " : " + newmyreg[i] + " Deleted\n");
			}
		}

		// Created
		for (int i = 0; i < hashList.size(); i += 2) {
			int sayac = 0;
			for (int j = 0; j < newmyreg.length; j += 2) {
				if (hashList.get(i).equals(newmyreg[j])) {

				} else {
					sayac++;
				}
			}
			if (sayac == newmyreg.length / 2) {
				w.write(dateFormat.format(cal.getTime()) + " : " + hashList.get(i) + " Created\n");
			}
		}

		w.close();
	}

	public static void createRegistry(String registryFile, ArrayList hashList, BigInteger enc, File registry)
			throws IOException {

		if (registry.exists()) {
			System.out.println("Registry.reg Already Exist!" + "\n");
			registry.delete();
		}

		File file = new File(registryFile);
		FileWriter writer = new FileWriter(file, true);
		BufferedWriter w = new BufferedWriter(writer);

		for (int i = 0; i < hashList.size() - 1; i += 2) {
			w.write(hashList.get(i) + " " + hashList.get(i + 1) + " ");
			w.write("\n");
		}
		w.write("Sign" + " " + enc);

		w.close();
	}

	public static String[] readRegistry(String reg, String[] myreg, File registry) throws IOException {
		String[] newmyreg = null;
		if (registry.exists()) {
			try (BufferedReader br = new BufferedReader(new FileReader(registry))) {
				String line;

				while ((line = br.readLine()) != null) {
					reg += line;
					myreg = reg.split(" ");
				}
				newmyreg = Arrays.copyOf(myreg, myreg.length - 2);
			}
			return newmyreg;
		} else {
			return newmyreg;
		}

	}

}
