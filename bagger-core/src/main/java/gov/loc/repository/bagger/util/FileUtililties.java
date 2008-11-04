package gov.loc.repository.bagger.util;

import gov.loc.repository.bagit.utilities.FilenameHelper;
import gov.loc.repository.bagger.bag.BaggerBag;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.channels.FileChannel;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import java.util.Collection;
import java.util.Iterator;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class FileUtililties {
	private static final Log log = LogFactory.getLog(FileUtililties.class);

	public static int BUFFER_SIZE = 10240;

	public static String createZip(BaggerBag baggerBag, File rootDir) {
		String errorMessage = null;
		File parentDir = rootDir.getParentFile();
		File zipFile = new File(parentDir, rootDir.getName() + ".zip");

		File[] list = listFilesAsArray(rootDir, null, true);
/*
 		for (int j=0; j<list.length; j++) {
			display("Bag.fileList: " + list[j].toString());
		}
 */
		// zip bag name
    	errorMessage = createZipArchive(rootDir.getParent(), zipFile, list);
		try {
			if (errorMessage == null) {
				String checksum = MD5Checksum.getMD5Checksum(zipFile.getAbsolutePath());
		    	baggerBag.setFile(zipFile);
		    	baggerBag.setSize(zipFile.length());
		    	//baggerBag.setPath(zipFile.getParent());
		    	//baggerBag.setChecksum(checksum);				
			}
		} catch (Exception e) {
			errorMessage = "Error occured creating checksum for zip file: " + zipFile.getName() + " ->" + e.getMessage();
			log.error("FileUtilities.createZip checksum: " + e);
		}
    	return errorMessage;
	}

	public static String createZipArchive(String parentPath, File archiveFile, File[] tobeZippedFiles) {
		try {
			byte buffer[] = new byte[BUFFER_SIZE];
			// Open archive file
			FileOutputStream stream = new FileOutputStream(archiveFile);
			ZipOutputStream out = new ZipOutputStream(stream);

			for (int i = 0; i < tobeZippedFiles.length; i++) {
				if (tobeZippedFiles[i] == null || !tobeZippedFiles[i].exists() || tobeZippedFiles[i].isDirectory())
					continue;
				
				display("Adding " + tobeZippedFiles[i].getName());

				// Add archive entry
				String normalizedFile = FilenameHelper.removeBasePath(parentPath, tobeZippedFiles[i].getAbsolutePath());
				display("Adding " + normalizedFile);
				ZipEntry zipAdd = new ZipEntry(normalizedFile);
				zipAdd.setTime(tobeZippedFiles[i].lastModified());
				out.putNextEntry(zipAdd);

				// Read input & write to output
				FileInputStream in = new FileInputStream(tobeZippedFiles[i]);
				while (true) {
					int nRead = in.read(buffer, 0, buffer.length);
					if (nRead <= 0)
						break;
					out.write(buffer, 0, nRead);
				}
				in.close();
			}

			out.close();
			stream.close();
			display("Adding completed OK");
		} catch (Exception e) {
			e.printStackTrace();
			log.error("Error: " + e.getMessage());
			return e.getMessage();
		}
		return null;
  }

  public static void createZipArchive(File archiveFile, Iterator<File> tobeZippedFiles) {
	  try {
		  byte buffer[] = new byte[BUFFER_SIZE];
	      // Open archive file
		  log.debug("createZipArchive: " + archiveFile);
	      FileOutputStream stream = new FileOutputStream(archiveFile);
	      ZipOutputStream out = new ZipOutputStream(stream);

	      for (Iterator<File> it=tobeZippedFiles; it.hasNext(); ) {
	    	  File f = it.next();
	    	  if (f == null || !f.exists() || f.isDirectory()) continue;
	    	  log.debug("Adding " + f.getName());
	    	  // Add archive entry
	    	  ZipEntry zipAdd = new ZipEntry(f.getName());
	    	  zipAdd.setTime(f.lastModified());
	    	  out.putNextEntry(zipAdd);

	    	  // Read input & write to output
	    	  FileInputStream in = new FileInputStream(f);
	    	  while (true) {
	    		  int nRead = in.read(buffer, 0, buffer.length);
		          	if (nRead <= 0)	break;
		          	out.write(buffer, 0, nRead);
	    	  }
	    	  in.close();
	      }
	      out.close();
	      stream.close();
	      log.debug("Adding completed OK");
	    } catch (Exception e) {
	      e.printStackTrace();
	      log.error("Error: " + e.getMessage());
	      return;
	    }
	  }

  static void listContents( File dir ) {
      // Assume that dir is a directory.  List
      // its contents, including the contents of
      // subdirectories at all levels.
  	String[] files;  // The names of the files in the directory.
  	files = dir.list();
  	for (int i = 0; i < files.length; i++) {
  		File f;  // One of the files in the directory.
  		f = new File(dir, files[i]);
  		if ( f.isDirectory() ) {
  			// Call listContents() recursively to
              // list the contents of the directory, f.
  			listContents(f);
  		}
     		else {
     			// For a regular file, just print the name, files[i].
     			display("Bag.listContents: " + files[i]);
     		}
 		}
 	} // end listContents()

  public static File[] listFilesAsArray(File directory, FilenameFilter filter, boolean recurse)
  {
  	Collection<File> files = listFiles(directory,filter, recurse);

  	File[] arr = new File[files.size()];
  	return files.toArray(arr);
  }

  public static Collection<File> listFiles(File directory,FilenameFilter filter,	boolean recurse)
  {
  	// List of files / directories
  	Vector<File> files = new Vector<File>();

  	// Get files / directories in the directory
  	File[] entries = directory.listFiles();

  	// Go over entries
  	for (File entry : entries)
  	{
  		// If there is no filter or the filter accepts the
  		// file / directory, add it to the list
  		if (filter == null || filter.accept(directory, entry.getName()))
  		{
  			files.add(entry);
  		}

  		// If the file is a directory and the recurse flag
  		// is set, recurse into the directory
  		if (recurse && entry.isDirectory())
  		{
  			files.addAll(listFiles(entry, filter, recurse));
  		}
  	}

  	// Return collection of files
  	return files;
  }

  public static void copyAFile(File source, File target) throws IOException
  { 
	  FileChannel sourceChannel = new FileInputStream(source).getChannel();
	  FileChannel targetChannel = new FileInputStream(target).getChannel();
	  sourceChannel.transferTo(0, sourceChannel.size(), targetChannel);
	  sourceChannel.close();
	  targetChannel.close();
  }
  
  /**
   * This function will copy files or directories from one location to another.
   * note that the source and the destination must be mutually exclusive. This 
   * function can not be used to copy a directory to a sub directory of itself.
   * The function will also have problems if the destination files already exist.
   * @param src -- A File object that represents the source for the copy
   * @param dest -- A File object that represnts the destination for the copy.
   * @throws IOException if unable to copy.
   */
  public static void copyFiles(File src, File dest) throws IOException {
  	//Check to ensure that the source is valid...
  	if (!src.exists()) {
  		log.error("FileUtilities.copyFiles: Can not find source: " + src.getAbsolutePath()+".");
  		throw new IOException("copyFiles: Can not find source: " + src.getAbsolutePath()+".");
  	} else if (!src.canRead()) { //check to ensure we have rights to the source...
  		log.error("FileUtilities.copyFiles: No right to source: " + src.getAbsolutePath()+".");
  		throw new IOException("copyFiles: No right to source: " + src.getAbsolutePath()+".");
  	}
  	//is this a directory copy?
  	if (src.isDirectory()) 	{
  		if (!dest.exists()) { //does the destination already exist?
  			//if not we need to make it exist if possible (note this is mkdirs not mkdir)
  			if (!dest.mkdirs()) {
  		  		log.error("FileUtilities.copyFiles: Could not create direcotry: " + dest.getAbsolutePath()+".");
  				throw new IOException("copyFiles: Could not create direcotry: " + dest.getAbsolutePath() + ".");
  			}
  		}
  		//get a listing of files...
  		String list[] = src.list();
  		//copy all the files in the list.
  		for (int i = 0; i < list.length; i++)
  		{
  			File dest1 = new File(dest, list[i]);
  			File src1 = new File(src, list[i]);
  			copyFiles(src1 , dest1);
  		}
  	} else { 
  		//This was not a directory, so lets just copy the file
  		FileInputStream fin = null;
  		FileOutputStream fout = null;
  		byte[] buffer = new byte[4096]; //Buffer 4K at a time (you can change this).
  		int bytesRead;
  		try {
  			//open the files for input and output
  			fin =  new FileInputStream(src);
  			fout = new FileOutputStream (dest);
  			//while bytesRead indicates a successful read, lets write...
  			while ((bytesRead = fin.read(buffer)) >= 0) {
  				fout.write(buffer,0,bytesRead);
  			}
  		} catch (IOException e) { //Error copying file... 
  			IOException wrapper = new IOException("copyFiles: Unable to copy file: " + 
  						src.getAbsolutePath() + " to " + dest.getAbsolutePath()+".");
  			wrapper.initCause(e);
  			wrapper.setStackTrace(e.getStackTrace());
  			throw wrapper;
  		} finally { //Ensure that the files are closed (if they were open).
  			if (fin != null) { fin.close(); }
  			if (fout != null) { fin.close(); }
  		}
  	}
  }
  
  public static void copyDirectory(File srcPath, File dstPath) throws IOException
  {
	  if (srcPath.isDirectory()){
		  if (!dstPath.exists()){
			  dstPath.mkdir();
		  }

		  String files[] = srcPath.list();
		  for(int i = 0; i < files.length; i++){
			  copyDirectory(new File(srcPath, files[i]), new File(dstPath, files[i]));
		  }
	  }
	  else{
		  if(!srcPath.exists()){
			  log.error("File or directory does not exist.");
			  //System.exit(0);
		  }
		  else
		  {
			  InputStream in = new FileInputStream(srcPath);
			  OutputStream out = new FileOutputStream(dstPath); 
			  // Transfer bytes from in to out
			  byte[] buf = new byte[1024];

			  int len;

			  while ((len = in.read(buf)) > 0) {
				  out.write(buf, 0, len);
			  }
			  in.close();
			  out.close();
		  }
	  }
	  log.info("Directory copied.");
  }

  	public static boolean deleteDir(File dir) {
		if (dir.isDirectory()) {
            String[] children = dir.list();
            for (int i=0; i<children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                	display("FileUtilities deleteDir unsuccessful: " + dir + "->" + children[i]);
                    return false;
                }
            }
        }
        // The directory is now empty so delete it
        return dir.delete();
    }
	
	public static void display(String s) {
		log.debug(s);
		//System.out.println(s);
	}
}
