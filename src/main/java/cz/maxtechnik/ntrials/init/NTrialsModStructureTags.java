package cz.maxtechnik.ntrials.init;

import cz.maxtechnik.ntrials.NTrialsMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.levelgen.structure.StructureSet;

public class NTrialsModStructureTags{

    public static final TagKey<StructureSet> TRIALS =
        TagKey.create(Registries.STRUCTURE_SET, 
                ResourceLocation.fromNamespaceAndPath(NTrialsMod.MODID, "trials"));
}