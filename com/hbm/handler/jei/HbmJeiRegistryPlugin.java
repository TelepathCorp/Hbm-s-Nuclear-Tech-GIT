package com.hbm.handler.jei;

import java.util.Collections;
import java.util.List;
import java.util.Timer;
import java.util.stream.Collectors;

import com.google.common.collect.Lists;
import com.hbm.handler.jei.JeiRecipes.AssemblerRecipeWrapper;
import com.hbm.items.tool.ItemAssemblyTemplate;
import com.hbm.lib.Library;

import mezz.jei.api.recipe.IFocus;
import mezz.jei.api.recipe.IFocus.Mode;
import mezz.jei.api.recipe.IRecipeCategory;
import mezz.jei.api.recipe.IRecipeRegistryPlugin;
import mezz.jei.api.recipe.IRecipeWrapper;
import net.minecraft.item.ItemStack;

public class HbmJeiRegistryPlugin implements IRecipeRegistryPlugin {

	// Drillgon200: This is needed because assembler recipes can change during
	// run time.
	@Override
	public <V> List<String> getRecipeCategoryUids(IFocus<V> focus) {
		return Lists.newArrayList(JEIConfig.ASSEMBLY);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends IRecipeWrapper, V> List<T> getRecipeWrappers(IRecipeCategory<T> recipeCategory, IFocus<V> focus) {
		if(focus.getValue() instanceof ItemStack) {
			ItemStack stack = (ItemStack) focus.getValue();
			if(JEIConfig.ASSEMBLY.equals(recipeCategory.getUid())) {
				if(focus.getMode() == Mode.INPUT) {
					long time = System.nanoTime();
					List<T> list = (List<T>) ItemAssemblyTemplate.recipes.stream().filter(recipe -> {
						for(ItemStack input : recipe.getInputs()) {
							if(Library.areItemStacksEqualIgnoreCount(input, stack))
								return true;
						}
						return false;
					}).map(recipe -> new AssemblerRecipeWrapper(recipe)).collect(Collectors.toList());
					System.out.println(System.nanoTime() - time);
					return list;
				} else if(focus.getMode() == Mode.OUTPUT) {
					return (List<T>) ItemAssemblyTemplate.recipes.stream().filter(recipe -> Library.areItemStacksEqualIgnoreCount(recipe.getOutput(), stack)).map(recipe -> new AssemblerRecipeWrapper(recipe)).collect(Collectors.toList());
				}
			}
		}
		return Collections.emptyList();
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends IRecipeWrapper> List<T> getRecipeWrappers(IRecipeCategory<T> recipeCategory) {
		if(recipeCategory.getUid().equals(JEIConfig.ASSEMBLY)) {
			return (List<T>) ItemAssemblyTemplate.recipes.stream().map(recipe -> new AssemblerRecipeWrapper(recipe)).collect(Collectors.toList());
		} else {
			return Collections.emptyList();
		}
	}

}