package com.dutchjelly.craftenhance;

import java.util.Arrays;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.plugin.java.JavaPlugin;

import com.dutchjelly.craftenhance.commandhandling.CustomCmdHandler;
import com.dutchjelly.craftenhance.commands.ChangeKeyCmd;
import com.dutchjelly.craftenhance.commands.CleanItemFileCmd;
import com.dutchjelly.craftenhance.commands.CreateRecipeCmd;
import com.dutchjelly.craftenhance.commands.OrderRecipesCmd;
import com.dutchjelly.craftenhance.commands.RecipesCmd;
import com.dutchjelly.craftenhance.commands.SetPermissionCmd;
import com.dutchjelly.craftenhance.commands.SpecsCommand;
import com.dutchjelly.craftenhance.crafthandling.EventClass;
import com.dutchjelly.craftenhance.crafthandling.RecipeInjector;
import com.dutchjelly.craftenhance.crafthandling.RecipeLoader;
import com.dutchjelly.craftenhance.data.ConfigFormatter;
import com.dutchjelly.craftenhance.data.FileManager;
import com.dutchjelly.craftenhance.gui.GUIContainer;
import com.dutchjelly.craftenhance.messaging.Debug;
import com.dutchjelly.craftenhance.messaging.Messenger;
import com.dutchjelly.craftenhance.util.GUIButtons;
import com.dutchjelly.craftenhance.util.Recipe;
import com.dutchjelly.itemcreation.commands.DisplayNameCmd;
import com.dutchjelly.itemcreation.commands.DurabilityCmd;
import com.dutchjelly.itemcreation.commands.EnchantCmd;
import com.dutchjelly.itemcreation.commands.ItemFlagCmd;
import com.dutchjelly.itemcreation.commands.LocalizedNameCmd;
import com.dutchjelly.itemcreation.commands.LoreCmd;

public class CraftEnhance extends JavaPlugin{
	
	//TODO handle 2*2 crafting
	//TODO Try to add categories.
	//TODO Clean up redundant setcancelled in GUIs onclick event.
	
	//Can be used in the future to use 1.13 functions with reflection.
	public static int getServerVersion(){
		char[] version = Bukkit.getBukkitVersion().toCharArray();
		int intVersion = 0;
		for(int i = 0; i < version.length; i++){
			if(version[i] == '-') break;
			if(!Character.isDigit(version[i])) continue;
			
			intVersion *= 10;
			intVersion += Character.getNumericValue(version[i]);
		}
		return intVersion;
	}
	
	
	private FileManager fm;
	private RecipeLoader loader;
	private GUIContainer guiContainer;
	private RecipeInjector injector;
	private CustomCmdHandler commandHandler;
	private Messenger messenger;
	
	@Override
	public void onEnable(){
		
		//The filemanager needs serialization, so firstly register the classes.
		registerSerialization(); 
		saveDefaultConfig();
		Debug.init(this);
		//Most other instances use the filemanager, so setup before everything.
		setupFileManager();
		
		GUIButtons.init();
		ConfigFormatter.init(this).formatConfigMessages();
		createInstances();
		loader.loadRecipes();
		setupListeners();
		setupCommands();
	}
	
	@Override
	public void onDisable(){
		guiContainer.closeAll();
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){
		commandHandler.handleCommand(sender, label, args);
		return true;
	}
	
	//Registers the classes that extend ConfigurationSerializable.
	private void registerSerialization(){
		ConfigurationSerialization.registerClass(Recipe.class, "Recipe");
	}
	
	//Create basic instances where order doesn't matter.
	private void createInstances(){
		loader = new RecipeLoader(this);
		guiContainer = new GUIContainer(this);
		injector = new RecipeInjector(fm);
		messenger = new Messenger(this);
	}
	
	//Assigns executor classes for the commands.
	private void setupCommands(){
		commandHandler = new CustomCmdHandler(this);
		//All commands with the base /edititem
		commandHandler.loadCommandClasses(Arrays.asList(new DisplayNameCmd(commandHandler), new DurabilityCmd(commandHandler),
				new EnchantCmd(commandHandler), new ItemFlagCmd(commandHandler), new LocalizedNameCmd(commandHandler), 
				new LoreCmd(commandHandler)));
		//All command with the base /ceh
		commandHandler.loadCommandClasses(Arrays.asList(new CreateRecipeCmd(commandHandler), new OrderRecipesCmd(commandHandler),
				new RecipesCmd(commandHandler), new SpecsCommand(commandHandler), new ChangeKeyCmd(commandHandler), 
				new CleanItemFileCmd(commandHandler), new SetPermissionCmd(commandHandler)));
		
	}
	
	//Registers the listener class to the server.
	private void setupListeners(){
		getServer().getPluginManager().registerEvents(new EventClass(this), this);
	}
	
	private void setupFileManager(){
		fm = FileManager.init(this);
		fm.cacheItems();
		fm.cacheRecipes();
	}
	
	public FileManager getFileManager(){
		return fm;
	}
	public RecipeLoader getRecipeLoader(){
		return loader;
	}
	public GUIContainer getGUIContainer(){
		return guiContainer;
	}
	public RecipeInjector getRecipeInjector(){
		return injector;
	}
	public Messenger getMessenger(){
		return messenger;
	}
	
	
}
