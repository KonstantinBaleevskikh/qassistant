package com.qassistant.context.entities;

import java.util.Objects;

public final class Context {
    private final Double distance;
    private final String content;
    private final String id;
    private final double weight;

    public Context(Double distance, String content, String id, double weight) {
        this.distance = distance;
        this.content = content;
        this.id = id;
        this.weight = weight;
    }

    public Double getDistance() {
        return this.distance;
    }

    public String getContent() {
        return this.content;
    }

    public String getId() {
        return this.id;
    }

    public double getWeight() {
        return this.weight;
    }

    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        Context context = (Context)obj;
        return Objects.equals(this.distance, context.distance) && Objects.equals(this.content, context.content) && Objects.equals(this.id, context.id) && Double.doubleToLongBits(this.weight) == Double.doubleToLongBits(context.weight);
    }

    public int hashCode() {
        return Objects.hash(this.distance, this.content, this.id, this.weight);
    }

    public String toString() {
        return "Context[distance=" + this.distance + ", content=" + this.content + ", id=" + this.id + ", weight=" + this.weight + "]";
    }
}
