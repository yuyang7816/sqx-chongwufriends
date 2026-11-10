UPDATE island_pet ip
INNER JOIN (
  SELECT user_id, MIN(pet_id) AS pet_id
  FROM user_pet
  WHERE status = 1 AND is_deleted = 0
  GROUP BY user_id
) up ON up.user_id = ip.user_id
SET ip.resident_pet_id = up.pet_id,
    ip.resident_pet_ids = CAST(up.pet_id AS CHAR)
WHERE ip.is_deleted = 0
  AND (ip.resident_pet_id IS NULL OR ip.resident_pet_id = 0);
