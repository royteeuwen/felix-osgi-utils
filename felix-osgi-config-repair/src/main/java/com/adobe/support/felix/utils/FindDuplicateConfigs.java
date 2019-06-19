package com.adobe.support.felix.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;

import javax.xml.bind.DatatypeConverter;

public class FindDuplicateConfigs {
	
	private static HashMap<String, ArrayList<String>> fileMap = new HashMap<String, ArrayList<String>>();
	private static final String OPTION_DELETE = "--delete"; 
	
	public static void main(String[] args) {
		File dir = null;
		boolean deleteDuplicateFiles = false;
		if (args.length > 0 && new File(args[0]).exists()) {
			if(args.length > 1) {
				if(OPTION_DELETE.equals(args[0])) {
					deleteDuplicateFiles = true;
					dir = new File(args[1]);
				} else {
					dir = new File(args[0]);
					if(OPTION_DELETE.equals(args[1])) {
						deleteDuplicateFiles = true;
					}
				}
			} else {
				dir = new File(args[0]);
			}
		} else {
			String jarName = "felix-osgi-config-repair.jar";
			System.out.println(
					"Usage:\n" 
					+ "    java -jar " + jarName + " /path/to/config/directory [--delete]" + "\n\n"
					+ "  e.g. output duplicate config files without deletion:" + "\n"
					+ "    java -jar " + jarName + " launchpad/config" + "\n\n"
					+ "  e.g. output duplicate config files with deletion:" + "\n"
					+ "    java -jar " + jarName + " launchpad/config --delete");
			return;
		}
		if(!deleteDuplicateFiles) {
			System.out.println("Note: No files will be deleted.");
		}
		
		findDuplicateFiles(dir, deleteDuplicateFiles);
	}

	private static void findDuplicateFiles(File dir, boolean deleteDuplicateFiles) {
		traverseAndBuildFileMap(dir);
		int duplicatesFound = 0;
		Iterator<String> keys = fileMap.keySet().iterator();
		while (keys.hasNext()) {
			String hash = keys.next();
			ArrayList<String> paths = fileMap.get(hash);
			if (paths.size() > 1) {
				Iterator<String> pathsIter = paths.iterator();
				boolean isFirst = true;
				while (pathsIter.hasNext()) {
					if (isFirst) {
						// Keep one of the group of duplicate factory configs
						System.out.println("Keeping: " + pathsIter.next());
						isFirst = false;
					} else {
						duplicatesFound++;
						// Delete the rest of the duplicate factory configs
						String pathToDelete = pathsIter.next();
						File fileToDelete = new File(pathToDelete);
						if (fileToDelete.exists()) {
							if (deleteDuplicateFiles)
								fileToDelete.delete();
						}
						System.out.println(((deleteDuplicateFiles)?"Deleted: ":"Would delete: ") + pathToDelete);
					}
				}
			}
		}
		if(duplicatesFound > 0) {
			System.out.println(duplicatesFound
					+ " duplicate config file" + ((duplicatesFound > 1)?"s":"")
					+" found" + ((deleteDuplicateFiles)?" and deleted.":"."));
		} else {
			System.out.println("No duplicate configurations found.");
		}
	}

	public static void traverseAndBuildFileMap(File dir) {
		File[] files = dir.listFiles();
		if (files != null) {
			for (File file : files) {
				if (file.isDirectory()) {
					checkFiles(file);
					traverseAndBuildFileMap(file);
				}
			}
		}
	}

	private static void checkFiles(File dir) {
		try {
			File[] files = dir.listFiles();
			if (files != null) {
				for (File file : files) {
					if (file.isFile()) {
						String digest = createHashOfConfigFileContents(file);
						
						// Use hash of file contents as key (to find duplicates)
						if (fileMap.containsKey(digest)) {
							ArrayList<String> arr = fileMap.get(digest);
							arr.add(file.getPath());
						} else {
							ArrayList<String> arr = new ArrayList<String>();
							arr.add(file.getPath());
							fileMap.put(digest, arr);
						}
					}
				}
			}
		} catch (IOException | NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
	}

	private static String createHashOfConfigFileContents(File file)
			throws IOException, FileNotFoundException, NoSuchAlgorithmException {
		ArrayList<String> fileContents = new ArrayList<String>();
		try (BufferedReader br = new BufferedReader(new FileReader(file))) {
			String line;
			while ((line = br.readLine()) != null) {
				if (isAllowed(line)) {
					// Regex test for factory config service.pid
					if (line.startsWith("service.pid") && line.matches(
							".*\\.[a-f0-9]{8}\\-[a-f0-9]{4}\\-[a-f0-9]{4}\\-[a-f0-9]{4}\\-[a-f0-9]{12}\"")) {
						// Include everything except for the unique extension of the factory pid
						// for example
						fileContents.add(line.substring(0, line.lastIndexOf('.')));
					} else {
						fileContents.add(line);
					}
				}
			}
		}
		Collections.sort(fileContents);
		MessageDigest md = MessageDigest.getInstance("MD5");
		Iterator<String> iter = fileContents.iterator();
		while (iter.hasNext()) {
			md.update(iter.next().getBytes());
		}
		String digest = DatatypeConverter.printHexBinary(md.digest());
		return digest;
	}

	private static boolean isAllowed(String line) {
		if (line == null || line.trim().equals("")) {
			return false;
		}
		return true;
	}
}
