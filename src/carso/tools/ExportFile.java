package carso.tools;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class ExportFile {

	// 检索目录
	private static String srcAddress = "";

	// 导出目录
	private static String desAddress = "";

	// 项目名称
	private static String projectName = "SilverBox";

	// 判断是否是要查找的文件分割字符串
	private static String splitSrcString = projectName + "\\\\src";
	private static String splitDesString = "WEB-INF\\\\classes";
	
	//文档注释符号
	private static String commentChar = "#";

	public static void main(String[] args) {
		System.out.println("----Start----");
		srcAddress = Util.getOneLineFromFile("resource/srcAddress.properties");
		desAddress = Util.getOneLineFromFile("resource/desAddress.properties");
		
		//读取目标文件列表
		File fileList = new File("resource/fileList.properties");

		try {
			InputStreamReader read = new InputStreamReader(new FileInputStream(fileList), Util.encoding);// 考虑到编码格式
			BufferedReader bufferedReader = new BufferedReader(read);
			String lineTxt = null;
			while ((lineTxt = bufferedReader.readLine()) != null) {
				// 读取文件列表中的文件
				if(!lineTxt.startsWith(commentChar)){
					File thisFile = new File(lineTxt);
					showAllFiles(thisFile);	
				}
			}
			read.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("----End----");
	}

	

	/**
	 * 遍历目录下所有文件,如果是文件夹则继续遍历，如果是文件就直接导出
	 * 
	 * @param dir
	 * @throws Exception
	 */
	private static void showAllFiles(File file) throws Exception {
		// 如果是文件夹则继续遍历
		if (file.isDirectory()) {
			File[] fs = file.listFiles();
			for (int i = 0; i < fs.length; i++) {
				if (fs[i].isDirectory()) {
					try {
						showAllFiles(fs[i]);
					} catch (Exception e) {

					}
				} else if (fs[i].isFile()) {
					System.out.println("查找以下文件并导出：" + fs[i].getAbsolutePath());
					exportFile(fs[i]);
				}
			}
		}
		// 如果是文件就直接导出
		else {
			exportFile(file);
		}

	}

	/**
	 * 复制文件到指定目录下
	 * 
	 * @param file
	 */
	private static void exportFile(File file) {
		List<File> fileList = new ArrayList<File>();
		// 首先去指定目录下查找文件
		Util.findFiles(srcAddress + "\\" + projectName, fileNameFilter(file.getName()), fileList);
		for(File desFile :fileList){
			// 复制文件到指定目录下
			try {
				Boolean copyFlg = false;
				//File desFile = new File(parentFolder.getAbsolutePath() + "\\" + srcFile.getName());
				//System.out.println("源文件地址" +file.getParent() );
				//System.out.println("找到文件地址" +desFile.getParent() );
				if(file.getAbsolutePath().indexOf(projectName) != -1){
					String parentFolderName = desFile.getParent();
					String[] pathArray = parentFolderName.split(projectName + "\\\\");
					// 取出文件的父路径，添加到目标目录地址下
					String desFolder = desAddress + "\\" + projectName + "\\" + pathArray[1];
					File parentFolder = new File(desFolder);
					//判断找到的这个文件是不是要查找的文件，对比两者的相对路径
					String[] srcArray = file.getParent().split(splitSrcString);
					String[] desArray = desFile.getParent().split(splitDesString);
					//如果是java文件，需要过滤一下是否有不同包名下重复的文件
					String extensionName = Util.getExtensionName(file.getName());
					if(extensionName.equals("java")){
						if(projectName.equals("reportinterface")){
							copyFlg = true;	
						}
						else if(srcArray[srcArray.length-1].equals(desArray[desArray.length-1])){
							copyFlg = true;	
						}		
					}
					else{
						copyFlg = true;
					}
					if(copyFlg){
						if (!parentFolder.exists()) {
							parentFolder.mkdirs();
						}
						System.out.println("查找成功，将以下文件进行复制：" + desFile.getAbsoluteFile().getAbsolutePath());
						copyFile(desFile, parentFolder);	
					}
					
				}
			} catch (Exception e) {
				e.printStackTrace();
			}	
		}
	}

	
	
	/**
	 * 过滤要匹配的文件
	 * 1：如果是.java文件，则查找对应的class文件
	 * 
	 * @param fileName
	 * @return
	 */
	private static String fileNameFilter(String fileName){
		String extensionName = Util.getExtensionName(fileName);
		if(extensionName.equals("java")){
			fileName = Util.getFileNameNoEx(fileName) + ".class";
		}
		return fileName;
	}

	/**
	 * 
	 * @param fileIn
	 *            要被copy的文件
	 * @param fileOutPut
	 *            将文件copy到那个目录下面 (此处为目录+要复制的文件名 例如C:\folder + \ + fileName)
	 * @throws Exception
	 */
	private static void copyFile(File fileIn, File fileOutPut) throws Exception {
		if(fileOutPut.isDirectory()){
			fileOutPut = new File(fileOutPut.getAbsolutePath() + "\\" + fileIn.getName());
		}
		FileInputStream fileInputStream = new FileInputStream(fileIn);
		FileOutputStream fileOutputStream = new FileOutputStream(fileOutPut);
		byte[] by = new byte[1024];
		int len;
		while ((len = fileInputStream.read(by)) != -1) {
			fileOutputStream.write(by, 0, len);
		}
		fileInputStream.close();
		fileOutputStream.close();
	}

	

}