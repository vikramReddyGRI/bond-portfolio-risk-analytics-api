
package com.financial.analytics.bonds.domain;

import java.util.List;

public class Portfolio {

    private final List<Bond> bonds;

    public Portfolio(List<Bond> bonds) {
        this.bonds = bonds;
    }

    public List<Bond> getBonds() {
        return bonds;
    }
}
