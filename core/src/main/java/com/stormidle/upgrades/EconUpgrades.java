package com.stormidle.upgrades;

import com.badlogic.gdx.utils.Array;

// Manages all "Econ" upgrade trees
// conversionTree - multiplies currency earned per bowl conversion
// condensationTree - generates currency passively over time

public class EconUpgrades {

    public final Array<UpgradeTier> conversionTree  = new Array<>();
    public final Array<UpgradeTier> condensationTree = new Array<>();

    public EconUpgrades() {
        buildConversionTree();
        buildCondensationTree();
    }

    // Each tier increases how much currency you get for bowl conversions
    private void buildConversionTree() {
        conversionTree.add(new UpgradeTier(
            UpgradeTier.TREE_CONVERSION, 1,
            "Silver Lining I",
            "Bowl conversions now yield 2 currency.",
            30,
            g -> g.currencyGained = 2
        ));
        conversionTree.add(new UpgradeTier(
            UpgradeTier.TREE_CONVERSION, 2,
            "Silver Lining II",
            "Bowl conversions now yield 5 currency.",
            300,
            g -> g.currencyGained = 5
        ));
        conversionTree.add(new UpgradeTier(
            UpgradeTier.TREE_CONVERSION, 3,
            "Silver Lining III",
            "Bowl conversions now yield 10 currency",
            3000,
            g -> g.currencyGained = 10
        ));
        conversionTree.add(new UpgradeTier(
            UpgradeTier.TREE_CONVERSION, 4,
            "Silver Lining IV",
            "Bowl conversions now yield 100 currency",
            300000,
            g -> g.currencyGained = 100
        ));
        conversionTree.add(new UpgradeTier(
            UpgradeTier.TREE_CONVERSION, 5,
            "Silver Lining V",
            "Bowl conversions now yield 1000 currency",
            30000000,
            g -> g.currencyGained = 1000
        ));
    }

    // Each tier increases passive cps (currency per second)
    // Tier 1 = 0.2 cps (1 per 5 seconds)
    private void buildCondensationTree() {
        condensationTree.add(new UpgradeTier(
            UpgradeTier.TREE_CONDENSATION, 1,
            "Condensation I",
            "Generate 1 currency every 5 seconds",
            50,
            g -> g.cps += 0.2f
        ));
        condensationTree.add(new UpgradeTier(
            UpgradeTier.TREE_CONDENSATION, 2,
            "Condensation II",
            "Generate 1 currency per 2 seconds",
            200,
            g -> g.cps += 0.5f
        ));
        condensationTree.add(new UpgradeTier(
            UpgradeTier.TREE_CONDENSATION, 3,
            "Condensation III",
            "Generate 1 currency per second",
            1000,
            g -> g.cps += 1f
        ));
        condensationTree.add(new UpgradeTier(
            UpgradeTier.TREE_CONDENSATION, 4,
            "Condensation IV",
            "Generate 10 currency per second",
            10000,
            g -> g.cps += 10f
        ));
        condensationTree.add(new UpgradeTier(
            UpgradeTier.TREE_CONDENSATION, 5,
            "Condensation V",
            "Generate 20 currency per second",
            1000000,
            g -> g.cps += 20f
        ));
    }
}
