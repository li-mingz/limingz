package com.limingz.mymod.mixins;

import com.limingz.mymod.util.SQLiteUtil;
import net.mcreator.caerulaarbor.block.SeaTrailGrownBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

@Mixin(SeaTrailGrownBlock.class)
public abstract class SeaTrailGrownBlockMixin extends Block implements SimpleWaterloggedBlock, BonemealableBlock{
    public SeaTrailGrownBlockMixin(Properties pProperties) {
        super(pProperties);
        System.out.println(this.getName());
    }
    @Inject(method = "onPlace", at = @At("HEAD"))
    public void onPlace(BlockState blockstate, Level world, BlockPos pos, BlockState oldState, boolean moving, CallbackInfo ci) {
        System.out.println("溟痕onPlace");
        Connection sqlConnection =  SQLiteUtil.getConnection();
        if(sqlConnection != null){
            try(sqlConnection;
                PreparedStatement pstmt = sqlConnection.prepareStatement(
                        "INSERT OR IGNORE INTO block_pos (x, y, z, name) VALUES (?,?,?,?)")){

                sqlConnection.setAutoCommit(false);  // 关闭自动提交
                pstmt.setInt(1, pos.getX());
                pstmt.setInt(2, pos.getY());
                pstmt.setInt(3, pos.getZ());
                pstmt.setString(4,  this.getDescriptionId());
                pstmt.executeUpdate();  // 插入
                sqlConnection.commit();  // 提交

            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
