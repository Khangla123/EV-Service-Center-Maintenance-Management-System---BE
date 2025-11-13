-- Populate selected_packages field with existing service package data
UPDATE service_appointments sa
SET selected_packages = jsonb_build_array(
    jsonb_build_object(
        'packageId', sa.service_package_id::text,
        'packageName', sp.name,
        'price', sp.price,
        'durationMinutes', sp.duration_minutes,
        'description', sp.description
    )
)
FROM service_packages sp
WHERE sa.service_package_id = sp.id
AND sa.service_package_id IS NOT NULL
AND (sa.selected_packages IS NULL OR sa.selected_packages = '[]'::jsonb);
