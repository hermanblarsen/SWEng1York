CREATE OR REPLACE FUNCTION notify_change() RETURNS TRIGGER AS $$
    BEGIN
        PERFORM pg_notify('db_update', TG_TABLE_NAME);
        RETURN NEW;
    END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER table_change 
    AFTER INSERT OR UPDATE OR DELETE ON users
    FOR EACH ROW EXECUTE PROCEDURE notify_change();

CREATE TRIGGER table_change 
    AFTER INSERT OR UPDATE OR DELETE ON presentation_library
    FOR EACH ROW EXECUTE PROCEDURE notify_change();

CREATE TRIGGER table_change 
    AFTER INSERT OR UPDATE OR DELETE ON classes
    FOR EACH ROW EXECUTE PROCEDURE notify_change();

-- End of file.

