package com.thom.datawriter;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.thom.datawriter.formatting.Category;
import com.thom.datawriter.formatting.Listing;
import com.thom.datawriter.formatting.SubCategory;
import com.thom.datawriter.formatting.TableListing;

public class DataWriter 
{	
	/**
	 * Generates a data file, no path. This means it will be created in the workspace directory.
	 */
	public void generateDataFile(DataFile file)
	{
		try 
		{
			FileWriter fw = new FileWriter(file.getName());
			writeLine(file, "@th-data file\n");
			fw.close();
		} 
		catch (IOException e) 
		{
			System.out.println("There was an error whilst trying to generate the data file: " + file.getName()); 
			e.printStackTrace();		
		}
	}
	
	/**
	 * Checks to see if a DataFile exists, no path. This means it will check in the workspace directory.
	 */
	public boolean doesFileExist(DataFile file)
	{
		if (file.exists() && !file.isDirectory())
			return true;
		else 
			return false;
	}

	/**
	 * Writes, or rather adds a new line of parameter 'text' to a specified DataFile.
	 */
	public void writeLine(DataFile file, String text) throws IOException
	{
		List<String> lines = getDataFileContents(file);
		
		lines.add(text);
		
		Files.write(file.toPath(), lines);
	}
	
	/**
	 * Sets the contents of a specified line in a DataFile to be parameter 'text'.
	 */
	public void setLine(DataFile file, int line, String text) throws IOException
	{
		List<String> lines = getDataFileContents(file);
		
		lines.set(line-1, text);
		
		Files.write(file.toPath(), lines);
	}
	
	/**
	 * Returns a specific line as a string in the specified DataFile.
	 */
	public String readLine(DataFile file, int line) throws IOException
	{
		return getDataFileContents(file).get(line-1);
	}
	
	/**
	 * Returns an ArrayList of integers which are all the digits found on the specific line in the specified DataFile.
	 */
	public ArrayList<Integer> getDigitsFromLine(DataFile file, int line) throws IOException
	{
		List<String> lines = getDataFileContents(file);
		
		String lineContents = lines.get(line-1);
		ArrayList<Integer> digits = new ArrayList<Integer>();
		
		for (int i = 0; i < lineContents.length(); i++)
		{
			if (Character.isDigit(lineContents.charAt(i)))
			{
				digits.add(Integer.valueOf(String.valueOf(lineContents.charAt(i))));
			}
		}
		return digits;
	}
	
	/**
	 * Returns an integer which is equal to putting all numbers in an ArrayList<Integer>, in a series of numbers.
	 * Example: "Player Health: 10hp". This example would return 10.
	 */
	public int getValueFromArrayList(ArrayList<Integer> arrayList)
	{
		String line = arrayList.toString().replace('[', ' ').replace(']', ' ').replace(',', ' ').replaceAll("\\s", "");
		
		if (line.isEmpty()) 	return 0; // The program crashes otherwise if getDigitsFromLine() is called on with an empty line as a parameter.
		else 					return Integer.valueOf(line); 
	}
	
	public List<String> getDataFileContents(DataFile file) throws IOException
	{
		return Files.readAllLines(file.toPath());
	}

	/**
	 * Returns the amount of lines in a specified DataFile.
	 */
	public int getLines(DataFile file) throws IOException 
	{
		return getDataFileContents(file).size();
	}
	
	/**
	 * Prints the amount of lines in a specified DataFile.
	 */
	public void countLines(DataFile file) throws IOException
	{
		System.out.println("Total lines: " + getLines(file) + ", in DataFile: " + file.getName());
	}
	
	/**
	 * =========================================== START OF ORGANIZED DATA WRITING ===========================================
	 */
	
	/**
	 * Initializes the category ArrayList<Category> with all the categories found in the specified DataFile.
	 * Also does this for all the sub categories as well as mapping what category the sub category is a child of.
	 */
	public void initializeCategories(DataFile file) throws IOException
	{
		List<String> lines = Files.readAllLines(file.toPath());
		
		for (int i = 0; i < lines.size(); i++)
		{
			if (lines.get(i).startsWith("#"))
			{
				file.addCategory(new Category(lines.get(i).substring(2, lines.get(i).length())));
			}
			
			//Creates a HashMap with the SubCategory as the key, the user can then retrieve the Category which the SubCategory is part of.
			if (lines.get(i).startsWith("-"))
			{
				HashMap<SubCategory, Category> categorySet = new HashMap<SubCategory, Category>();
				SubCategory subCategory = new SubCategory(lines.get(i).substring(2, lines.get(i).length())); 
		
				file.addSubCategory(subCategory);
				subCategory.setCategory(getCategoryFromLine(file, getCategoryLineFromSubCategory(file, subCategory, i)));
				System.out.println("Initialized SubCategory: " + subCategory.getSubCategoryName() + ", to the Category: " + getCategoryFromLine(file, getCategoryLineFromSubCategory(file, subCategory, i)).getCategoryName());
				
				// Adds a <K,V> set, to the HashMap by first taking SubCategory found, and then the Category from that SubCategory is retrieved via the line number of the SubCategory using the getCategoryLineFromSubCategory() Method.
				categorySet.put(subCategory, getCategoryFromLine(file, getCategoryLineFromSubCategory(file, subCategory, i)));
				// Adds the HashMap to the DataFile's ArrayList of SubCategory, Category HashMaps.
				file.map.add(categorySet); 
			}
		}
	}
	
	/**
	 * Returns the line number (index) of a Category, retrieved using the SubCategory's line number in the specified DataFile.
	 * This method is used when initializing the read Data from the DataFile. See initializeCategories(DataFile file) for more.
	 */
	public int getCategoryLineFromSubCategory(DataFile file, SubCategory subCategory, int subCategoryLineNumber) throws IOException
	{
		List<String> lines = getDataFileContents(file);
	
		for (int i = 0; i < subCategoryLineNumber; i++)
		{
			if (lines.get(subCategoryLineNumber-i).startsWith("#"))
			{
				return subCategoryLineNumber-i+1;
			}
		}
		return 0;
	}
	
	public Category getCategoryFromLine(DataFile file, int lineNumber) throws IOException
	{
		for (int i = 0; i < file.categories.size(); i++)
		{
			String category = "# " + file.categories.get(i).getCategoryName();
			
			if (category.equals(readLine(file, lineNumber)))
			{
				return file.categories.get(i);
			}
		}
		return null;
	}
	
	/**
	 * Adds a category to the ArrayList<Category> in the specified DataFile.
	 * This also does the actual writing in the DataFile.
	 */
	public void addCategory(DataFile file, Category category) throws IOException
	{
		if (!doesCategoryWithNameExist(file, category.getCategoryName()))
		{
			file.addCategory(category);
			this.writeLine(file, "# " + category.getCategoryName() + "\n");
		}
		else
		{
			System.out.println("The Category '" + category.getCategoryName() + "' already exists within the DataFile: " + file.getName());
		}
	}
	
	/**
	 * Returns whether or not a category exists within the specified DataFile.
	 */
	public boolean doesCategoryWithNameExist(DataFile file, String categoryName) throws IOException
	{
		for (int i = 0; i < getLines(file); i++)
		{
			if (readLine(file, i+1).startsWith("#"))
			{
				String currentCategoryName = readLine(file, i+1).substring(2, readLine(file, i+1).length());
				
				if (currentCategoryName.equalsIgnoreCase(categoryName)) 
				{
					return true;
				}
			}
		}
		return false;
	}
	
	/**
	 * Adds a sub category to the ArrayList<SubCategory> in the specified DataFile. As well as adds the sub category to a category.
	 * This also does the actual writing in the DataFile.
	 */
	public void addSubCategory(DataFile file, Category category, SubCategory subCategory) throws IOException
	{
		if (!doesSubCategoryWithNameExist(file, subCategory.getSubCategoryName()))
		{
			subCategory.setCategory(category);
			file.addSubCategory(subCategory);

			this.setLine(file, getLineForNewObject(file, subCategory, category, null, null), "\n- " + subCategory.getSubCategoryName() + "\n");
		}
		else
		{
			System.out.println("The Sub-Category '" + subCategory.getSubCategoryName() + "' already exists within the DataFile: " + file.getName());
		}
	}
	
	/**
	 * Returns whether or not a sub category exists within the specified DataFile.
	 */
	public boolean doesSubCategoryWithNameExist(DataFile file, String subCategoryName) throws IOException
	{
		for (int i = 0; i < getLines(file); i++)
		{
			if (readLine(file, i+1).startsWith("-"))
			{
				String currentSubCategoryName = readLine(file, i+1).substring(2, readLine(file, i+1).length());
				
				if (currentSubCategoryName.equalsIgnoreCase(subCategoryName)) 
				{
					return true;
				}
			}
		}
		return false;
	}
	
	/**
	 * Creates a Listing under a Category or SubCategory in the specified DataFile.
	 */
	public void addListing(DataFile file, Category category, SubCategory subCategory, Listing listing) throws IOException
	{
		setLine(file, getLineForNewObject(file, listing, category, subCategory, null), listing.getContents() + "\n");
	}
	
	/**
	 * Creates a Listing under a Category or SubCategory inside a TableListing in the specified DataFile.
	 */
	/*public void addListing(DataFile file, Category category, SubCategory subCategory, TableListing tableListing, Listing listing) throws IOException
	{
		setLine(file, getLineForNewObject(file, category, subCategory, tableListing), listing.getContents() + "\n");
	}*/
	
	/**
	 * Returns a line (index) for a new Object to be placed at in either a Category, SubCategory or TableListing in the specified DataFile.
	 */
	public int getLineForNewObject(DataFile file, Object objToAdd, Category category, SubCategory subCategory, TableListing tableListing) throws IOException
	{
		List<String> lines = getDataFileContents(file);
		
		if (category != null)
		{
			if (tableListing == null)
			{
				// Adding to a Category
				if (subCategory == null)
				{	
					for (int i = 0; i < file.categories.size(); i++)
					{
						if (file.categories.get(i).getCategoryName().equals(category.getCategoryName()))
						{
							if (file.categories.size() > i+1)	
							{
								return getCategoryLine(file, file.categories.get(i+1));
							}
							else
							{
								return lines.size();
							}
						}
						else { System.out.println("The Category you specified does not exist within the DataFile: " + file.getName()); }
					}
				}
				// Adding to a SubCategory
				else
				{
					// Checking if the subcategory exists
					for (int i = 0; i < file.subCategories.size(); i++)
					{
						if (file.subCategories.get(i).getSubCategoryName().equals(subCategory.getSubCategoryName()))
						{
							System.out.println(subCategory.getCategory().getCategoryName());
//							if (subCategory.getCategory().getCategoryName().equals(category.getCategoryName()))
//							{
//								return getSubCategoryLine(file, subCategory)+2;
//							}
//							else { System.out.println("The SubCategory you specified does not exist within the Category: " + category.getCategoryName()); }
						}
						else { System.out.println("The SubCategory you specified does not exist within the DataFile: " + file.getName()); }
					}
				}
			}
			// Adding to a TableListing
			else
			{
				
			}
		}
		// Adding outside Formatting Objects
		else
		{
			// Make it so you can't add Listing's to Categories that uses SubCategories.
			return lines.size();
		}
		System.out.println("Failed");
		return 0;
	}
	
	public boolean isLineListing(DataFile file, int lineNumber) throws IOException
	{
		List<String> lines = getDataFileContents(file);
		String line = lines.get(lineNumber);
		
		if (line.startsWith("#") || line.startsWith("-") || line.isEmpty() || line.equals("@th-data file"))
		{
			return false;
		}
		else
		{
			return true;
		}
	}
	
	public int getCategoryLine(DataFile file, Category category) throws IOException
	{
		List<String> lines = getDataFileContents(file);
		
		for (int i = 0; i < lines.size(); i++)
		{
			if (lines.get(i).equals("# " + category.getCategoryName()))
			{
				return i;
			}
		}
		return 0;
	}
	
	public int getSubCategoryLine(DataFile file, SubCategory subCategory) throws IOException
	{
		List<String> lines = getDataFileContents(file);
		
		for (int i = 0; i < lines.size(); i++)
		{
			if (lines.get(i).equals("- " + subCategory.getSubCategoryName()))
			{
				return i+1;
			}
		}
		return 0;
	}
	
	/*public int getTableListingLine(DataFile file, TableListing tableListing)
	{
		List<String> lines = getDataFileContents(file);
		
		
	}*/
	
	/*public void addTableListing(DataFile file, Category category, SubCategory subCategory, TableListing tableListing)
	{
		setLine(file, getLineForNewListingInCategory(file, category, subCategory), listing.getContents() + "\n");
	}*/
}