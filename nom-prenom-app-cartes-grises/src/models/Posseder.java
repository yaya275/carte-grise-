package models;

import config.DBConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Classe représentant la possession d’un véhicule par un propriétaire
 * --------------------------------------------------------------------
 * Objectifs pédagogiques BTS SIO :
 * 1. Comprendre les relations many-to-many entre propriétaires et véhicules.
 * 2. Manipuler les clés étrangères (idProprietaire et idVehicule).
 * 3. Gérer les dates de début et fin de possession avec java.sql.Date.
 * 4. Effectuer des opérations CRUD via JDBC (INSERT, UPDATE, DELETE, SELECT).
 * 5. Utiliser PreparedStatement pour sécuriser les requêtes SQL.
 * 6. Retourner des listes d’objets pour affichage dans une JTable.
 * 7. Vérifier l'existence d'un enregistrement avant modification/suppression
 * (logique métier).
 */
public class Posseder {

    private int idProprietaire;
    private int idVehicule;
    private Date dateDebut;
    private Date dateFin;

    // ================== GETTERS / SETTERS ==================
    /**
     * Objectif pédagogique : apprendre à encapsuler les attributs et fournir
     * des accesseurs (getters/setters) pour manipuler les données d'un modèle.
     */
    public int getIdProprietaire() {
        return idProprietaire;
    }

    public void setIdProprietaire(int idProprietaire) {
        this.idProprietaire = idProprietaire;
    }

    public int getIdVehicule() {
        return idVehicule;
    }

    public void setIdVehicule(int idVehicule) {
        this.idVehicule = idVehicule;
    }

    public Date getDateDebut() {
        return dateDebut;
    }

    public void setDateDebut(Date dateDebut) {
        this.dateDebut = dateDebut;
    }

    public Date getDateFin() {
        return dateFin;
    }

    public void setDateFin(Date dateFin) {
        this.dateFin = dateFin;
    }

    // ================== DAO / CRUD ==================

    /**
     * Récupérer toutes les possessions
     * --------------------------------
     * Objectifs pédagogiques :
     * - Expliquer la construction d'une requête SELECT.
     * - Parcourir un ResultSet pour construire des objets métier.
     * - Gérer les ressources JDBC avec try-with-resources.
     *
     * return liste de toutes les possessions
     */
    public static List<Posseder> getAllPossessions() {
        List<Posseder> liste = new ArrayList<>();
        String sql = "SELECT * FROM POSSEDER";

        try (Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Posseder p = new Posseder();
                p.setIdProprietaire(rs.getInt("id_proprietaire"));
                p.setIdVehicule(rs.getInt("id_vehicule"));
                p.setDateDebut(rs.getDate("date_debut_propriete"));
                p.setDateFin(rs.getDate("date_fin_propriete"));
                liste.add(p);
            }

        } catch (SQLException e) {
            System.err.println("Erreur getAllPossessions : " + e.getMessage());
        }

        return liste;
    }

    /**
     * Vérifie si une possession existe déjà
     * --------------------------------------
     * Objectifs pédagogiques :
     * - Utiliser COUNT(*) pour tester l'existence d'un enregistrement.
     * - Prévenir les doublons au niveau applicatif.
     *
     * param idProprietaire identifiant du propriétaire
     * param idVehicule identifiant du véhicule
     * return true si la possession existe, false sinon
     */
    public static boolean existsPossession(int idProprietaire, int idVehicule) {
        String sql = "SELECT COUNT(*) FROM POSSEDER WHERE id_proprietaire=? AND id_vehicule=?";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idProprietaire);
            ps.setInt(2, idVehicule);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next())
                    return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.err.println("Erreur existsPossession : " + e.getMessage());
        }
        return false;
    }

    /**
     * Ajouter une possession
     * ----------------------
     * Objectifs pédagogiques :
     * - Construire et exécuter une requête INSERT via PreparedStatement.
     * - Protéger l'application contre les doublons en vérifiant l'existence avant
     * insertion.
     * - Manipuler les types java.sql.Date pour stocker des dates en base.
     *
     * param idProprietaire identifiant du propriétaire
     * param idVehicule identifiant du véhicule
     * param dateDebut date de début de possession
     * param dateFin date de fin de possession
     * return true si l'ajout a réussi, false sinon
     */
    public static boolean addPossession(int idProprietaire, int idVehicule, Date dateDebut, Date dateFin) {
        if (existsPossession(idProprietaire, idVehicule))
            return false;

        String sql = "INSERT INTO POSSEDER (id_proprietaire, id_vehicule, date_debut_propriete, date_fin_propriete) VALUES (?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, idProprietaire);
            ps.setInt(2, idVehicule);
            ps.setDate(3, dateDebut);
            ps.setDate(4, dateFin);

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Erreur addPossession : " + e.getMessage());
            return false;
        }
    }

    /**
     * Modifier une possession (dates uniquement)
     * ------------------------------------------
     * Objectifs pédagogiques :
     * - Comprendre la logique de mise à jour (UPDATE) ciblée par clé composée.
     * - Vérifier l'existence avant modification pour éviter les erreurs métier.
     * - Adapter les paramètres pour correspondre au contrôleur (idProprietaire,
     * idVehicule, dateDebut, dateFin).
     *
     * param idProprietaire identifiant du propriétaire (clé composée)
     * param idVehicule identifiant du véhicule (clé composée)
     * param dateDebut nouvelle date de début
     * param dateFin nouvelle date de fin
     * return true si la modification a réussi, false sinon
     */
    public static boolean updatePossession(int idProprietaire, int idVehicule, Date dateDebut, Date dateFin) {
        String sql = "UPDATE POSSEDER SET date_debut_propriete=?, date_fin_propriete=? WHERE id_proprietaire=? AND id_vehicule=?";

        try (Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setDate(1, dateDebut);
            ps.setDate(2, dateFin);
            ps.setInt(3, idProprietaire);
            ps.setInt(4, idVehicule);

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Erreur updatePossession : " + e.getMessage());
            return false;
        }
    }

    /**
     * Supprimer une possession
     * ------------------------
     * Objectifs pédagogiques :
     * - Expliquer la suppression conditionnelle via DELETE.
     * - Vérifier l'existence avant suppression pour montrer une bonne pratique
     * métier.
     *
     * param idProprietaire identifiant du propriétaire
     * param idVehicule identifiant du véhicule
     * return true si la suppression a réussi, false sinon
     */
    public static boolean deletePossession(int idProprietaire, int idVehicule) {
        String deleteSql = "DELETE FROM POSSEDER WHERE id_proprietaire = ? AND id_vehicule = ?";

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);

            try (PreparedStatement psDelete = conn.prepareStatement(deleteSql)) {
                psDelete.setInt(1, idProprietaire);
                psDelete.setInt(2, idVehicule);
                if (psDelete.executeUpdate() > 0) {
                    conn.commit();
                    return true;
                } else
                    conn.rollback();
            }

        } catch (SQLException e) {
            System.err.println("Erreur deletePossession : " + e.getMessage());
        }

        return false;
    }
}