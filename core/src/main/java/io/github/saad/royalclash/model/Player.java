package io.github.saad.royalclash.model;

/** One side of the match. Used for both you and the AI. */
public class Player {
    public static final float MAX_ELIXIR = 10f;

    private final Team team;
    private final Deck deck;
    private float elixir = 5f;
    private int crowns;

    public Player(Team team, Deck deck) {
        this.team = team;
        this.deck = deck;
    }

    public Team getTeam() { return team; }
    public Deck getDeck() { return deck; }
    public float getElixir() { return elixir; }
    public int getCrowns() { return crowns; }
    public boolean canAfford(Card card) { return elixir >= card.getElixirCost(); }

    void addElixir(float amount) { elixir = Math.min(MAX_ELIXIR, elixir + amount); }
    void spendElixir(int amount) { elixir -= amount; }
    void addCrown() { crowns++; }
    void setCrowns(int crowns) { this.crowns = crowns; }
}
