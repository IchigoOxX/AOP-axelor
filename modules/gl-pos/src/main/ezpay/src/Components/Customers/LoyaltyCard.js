import React from 'react';
import { Card, CardContent, Typography, Avatar, Box } from '@mui/material';
import PersonIcon from '@mui/icons-material/Person';
import styled from '@emotion/styled';

const StyledCard = styled(Card)`
  width: 230px;
  margin: 10px;
  padding: 10px;
  display: flex;
  border: 1px solid gray;
  align-items: center;
  box-shadow: 0 4px 2px rgba(0, 0, 0, 0.2);
`;

const InfoBox = styled(Box)`
  margin-left: 20px;
`;

const PointsBox = styled(Box)`
  margin-top: 10px;
  background-color: #f5f5f5;
  padding: 10px;
  border-radius: 5px;
`;

const LoyaltyCard = ({ name, loyaltyCard }) => {
  return (
    <StyledCard>
      <Avatar>
        <PersonIcon />
      </Avatar>
      <InfoBox>
        <Typography variant="h6">{name}</Typography>
        <PointsBox>
          <Typography variant="body2">Points: {loyaltyCard.points}</Typography>
        </PointsBox>
      </InfoBox>
    </StyledCard>
  );
};

export default LoyaltyCard;
