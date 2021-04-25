package com.epam.ik.logic;

import com.epam.ik.entity.Position;

import java.util.List;

public interface Movement {

    List<List<Position>> deriveAllMoves();

}
