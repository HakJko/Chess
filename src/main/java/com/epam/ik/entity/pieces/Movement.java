package com.epam.ik.entity.pieces;

import com.epam.ik.entity.Position;

import java.util.List;

public interface Movement {

    List<List<Position>> deriveAllMoves();

}
