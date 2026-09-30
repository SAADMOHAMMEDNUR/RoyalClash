package io.github.saad.royalclash.model;

/**
 * The blueprint. Stats never change mid-match, so everything is final and there are no setters.
 *
 */
public class Card {
    private final String name;
    private final String label;          // short text drawn on the square
    private final int elixirCost;
    private final int maxHP;
    private final int damage;
    private final float range;           // reach, measured edge to edge
    private final float size;
    private final float speed;           // units per second
    private final float attackSpeed;     // seconds between hits
    private final int count;             // how many troops spawn (Archers = 2, Skeletons = 3)
    private final boolean targetsBuildingsOnly;
    private final boolean building;

    public Card(String name, String label, int elixirCost, int maxHP, int damage, float range, float size,
                float speed, float attackSpeed, int count, boolean targetsBuildingsOnly, boolean building) {
        this.name = name;
        this.label = label;
        this.elixirCost = elixirCost;
        this.maxHP = maxHP;
        this.damage = damage;
        this.range = range;
        this.size = size;
        this.speed = speed;
        this.attackSpeed = attackSpeed;
        this.count = count;
        this.targetsBuildingsOnly = targetsBuildingsOnly;
        this.building = building;
    }

    public String getName() { return name; }
    public String getLabel() { return label; }
    public int getElixirCost() { return elixirCost; }
    public int getMaxHP() { return maxHP; }
    public int getDamage() { return damage; }
    public float getRange() { return range; }
    public float getSize() { return size; }
    public float getSpeed() { return speed; }
    public float getAttackSpeed() { return attackSpeed; }
    public int getCount() { return count; }
    public boolean targetsBuildingsOnly() { return targetsBuildingsOnly; }
    public boolean isBuilding() { return building; }
}
